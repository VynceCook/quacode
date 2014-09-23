/* -*- mode: C++; c-basic-offset: 2; indent-tabs-mode: nil -*- */
/*
 *  Main authors:
 *     Vincent Barichard <Vincent.Barichard@univ-angers.fr>
 *
 *  Copyright:
 *     Vincent Barichard, 2013
 *
 *  Last modified:
 *     $Date$ by $Author$
 *     $Revision$
 *
 *  This file is part of Quacode:
 *     http://quacode.barichard.com
 *
 *  Permission is hereby granted, free of charge, to any person obtaining
 *  a copy of this software and associated documentation files (the
 *  "Software"), to deal in the Software without restriction, including
 *  without limitation the rights to use, copy, modify, merge, publish,
 *  distribute, sublicense, and/or sell copies of the Software, and to
 *  permit persons to whom the Software is furnished to do so, subject to
 *  the following conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 *  LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 *  OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 *  WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

#include <gecode/support.hh>

#ifdef GECODE_HAS_THREADS

#include <quacode/search/parallel/qdfs.hh>

namespace Gecode { namespace Search { namespace Parallel {

  /*
   * Statistics
   */
  Statistics 
  QDFS::statistics(void) const {
    Statistics s;
    for (unsigned int i=0; i<workers(); i++)
      s += worker(i)->statistics();
    return s;
  }


  /*
   * Engine: search control
   */
  void
  QDFS::Worker::run(void) {
    Space * solvedSpace = NULL;
    TQuantifier bckQuant = EXISTS;
    // Peform initial delay, if not first worker
    if (this != engine().worker(0))
      Support::Thread::sleep(Config::initial_delay);
    // Okay, we are in business, start working
    while (true) {
      m.acquire();
      switch (engine().cmd()) {
      case C_WAIT:
        m.release();
        // Wait
        engine().wait();
        break;
      case C_TERMINATE:
        m.release();
        // Acknowledge termination request
        engine().ack_terminate();
        // Wait until termination can proceed
        engine().wait_terminate();
        // Terminate thread
        engine().terminated();
        return;
      case C_RESET:
        m.release();
        // Acknowledge reset request
        engine().ack_reset_start();
        // Wait until reset has been performed
        engine().wait_reset();
        if (solvedSpace)
        {
          delete solvedSpace;
          solvedSpace = NULL;
        }
        // Acknowledge that reset cycle is over
        engine().ack_reset_stop();
        break;
      case C_WORK:
        // Perform usual work
        {
          // Update owner if needed.
#ifdef LOG_AUDIT      
          if (updateOwner())
            LOG_OUTN(_id,"UPDATED OWNER to " << _ownerId << " workId=" << _workId << "\n");
#else      
          (void) updateOwner();
#endif      
 
          // This guy giving-up its path
          if (qpath.givingUp() == QPath::F_GIVING_UP_YES) {
            LOG_OUTN(_id,"GIVING-UP path (wait)\n");
            m.release();
            // This guy is waiting for one of its thief so it transfers
            // its path to it and that every other thiefs update their owner
            _e_wait_path.wait();

            m.acquire();
            LOG_OUTN(_id,"GIVING-UP path (end)\n");

            // Finish path transfer (reset)
            assert(qpath.empty());
            assert(qpath.data().nbCommitWork() == 0);
            assert(qpath.data().nbCanceledWork() == 0);
            reset(NULL,0);
            if (solvedSpace)
            {
              delete solvedSpace;
              solvedSpace = NULL;
            }
            idle = true;
            m.release();
            // Report that worker is idle
            engine().idle();
          } else m.release();

          // This guy has to reset
          m.acquire();
          if (workCanceled()) {
            LOG_OUTN(_id,"RESET WorkId=" << _workId << " has been canceled from owner " << _ownerId << "\n");
            // Our owner has cut the branch which carried our work,
            // work is not longer needed, we reset
            assert(_newOwnerId == -1);
            assert(_n_not_updated_owner == 0);
            qpath.data().clearCommitWork();
            // We keep _workCanceled in order to thief of this guy can
            reset(NULL,0);
            if (solvedSpace)
            {
              delete solvedSpace;
              solvedSpace = NULL;
            }
            // reset themselves.
            idle = true;
            m.release();
            // Report that worker is idle
            engine().idle();
          } else m.release();

          // This guy commit pending works
          m.acquire();
          QWorkData::Work wo;
          bool hasCut = false;
          while (qpath.data().getCommitWork(wo)) {
            hasCut = qpath.cut(wo.workId,wo.solution,bckQuant) || hasCut;
          }
          if (hasCut && (cur != NULL)) {
            delete cur;
            cur = NULL;
          }
          // Finish commit
          m.release();

          m.acquire();
          // This guy want to take path from its owner
          if (ownerGivingUpPath()) {
            LOG_OUTN(_id,"TAKE PATH from " << _ownerId << " workId=" << _workId << "\n");
            // Our owner want to transfer its path, this guy acknowledge
            // Worker has to retrive path from its owner
            assert(_ownerId != -1);
            QDFS::Worker& WOwner = *static_cast<QDFS::Worker*>(engine().worker(_ownerId));
            // Block this guy and owner
            WOwner._m_id.acquire();
            _m_id.acquire();

            unsigned int nbThiefs = 0;
            qpath.takePath(_workId, nbThiefs, WOwner.qpath);
            assert(WOwner.qpath.empty());

            // Update owner information
            WOwner._newOwnerId = _id;
            WOwner._n_not_updated_owner = nbThiefs;

            // From now, no worker will commit to this owner guy because
            // _newOwnerId has been set
            // Update information
            _ownerId = WOwner.owner();
            _workId = WOwner.work();

            // Release us and owner
            _m_id.release();
            WOwner._m_id.release();

            // Free owner, if no pending thief of owner (but us) has
            // been found.
            if (nbThiefs == 0) WOwner._e_wait_path.signal();
            LOG_OUTN(_id,"TAKE PATH (end)\n");
          }
          m.release();

          m.acquire();
          if (idle) {
            m.release();
            // Try to find new work
            find();
          } else if (cur != NULL) {
            start();
            if (stop(engine().opt())) {
              // Report stop
              m.release();
              engine().stop();
            } else {
              node++;
              SpaceStatus curStatus = cur->status(*this);
              // Si plus de propagateurs dans l'espace alors tout est succès dessous,
              // il faut donc le traiter comme un cas de succès
              if ((!cur->failed()) && (cur->propagators() == 0) && (cur->branchers() == 0)) curStatus = SS_SOLVED;

              switch (curStatus) {
              case SS_FAILED:
                // On devra dépiler jusqu'au dernier existentiel
                bckQuant = EXISTS;
                fail++;
                delete cur;
                cur = NULL;
                m.release();
                break;
              case SS_SOLVED:
                // On devra dépiler jusqu'au dernier universel
                bckQuant = FORALL;
                {
                  // Deletes all pending branchers
                  (void) cur->choice();
                  solvedSpace = cur->clone(false);
                  delete cur;
                  cur = NULL;
                  m.release();
                }
                break;
              case SS_BRANCH:
                {
                  Space* c;
                  if ((d == 0) || (d >= engine().opt().c_d)) {
                    c = cur->clone();
                    d = 1;
                  } else {
                    c = NULL;
                    d++;
                  }
                  const Choice* ch = qpath.push(*this,cur,c);
                  cur->commit(*ch,0);
                  m.release();
                }
                break;
              default:
                GECODE_NEVER;
              }
            }
          } else {
            if (qpath.next(bckQuant)) {
              cur = qpath.recompute(d,engine().opt().a_d,*this);
              if (solvedSpace)
              {
                delete solvedSpace;
                solvedSpace = NULL;
              }
              m.release();
            } else {
              bool goIdle = false;
              if (_workId == 0) { // No work owned, we just go to idle
                goIdle = true;
              } else if (qpath.givingUp() == QPath::F_GIVING_UP_NO) {
                assert(qpath.empty());
                // This guy commits its work
                switch (commit(solvedSpace != NULL)) {
                case QEngine::ER_CANCELED:
                  // Work has been canceled by owner we just have to reset.
                  // During commit call, the work has been removed from list
                  // of work to cancel of owner.
                  LOG_OUTN(_id,"COMMIT FAILED WorkId=" << _workId << ", has been canceled by owner " << _ownerId << "\n");
                case QEngine::ER_NONE:
                  LOG_OUTN(_id,"COMMIT SUCCEDEED WorkId=" << _workId << ", to owner " << _ownerId << "\n");
                  // Commit suceeded
                  reset(NULL,0);
                  goIdle = true;
                  break;
                case QEngine::ER_NO_OWNER:
                  assert(_workId == 1);
                  // Commit failed because this guy hold the path.
                  // So it answers if solution or failure (end of search)
                  LOG_OUTN(_id,"COMMIT FAILED WorkId=" << _workId << ", no owner END of search\n");
                  if (solvedSpace) {
                    engine().solution(solvedSpace);
                    solvedSpace = NULL;
                  }
                  goIdle = true;
                  break;
                case QEngine::ER_OWNER_CHANGED:
                  // Owner changed before commit. This guy will go next loop,
                  // will update its owner and will try again to commit as
                  // cur == NULL and qpath is empty
                  assert(cur == NULL);
                  LOG_OUTN(_id,"COMMIT FAILED WorkId=" << _workId << ", owner " << _ownerId << " has changed\n");
                  break;
                case QEngine::ER_CANDIDATE:
                  // This guy will go next loop. If it doesn't take the path,
                  // it will be removed from list of candidate and will try
                  // again to commit as cur == NULL and qpath is empty. If it
                  // take the path of its owner, it will try to backtrack again
                  // as cur == NULL. As bckQuant hasn't change it will looking
                  // for the same kind of quantifier and as solvedSpace is not
                  // changed, if an instance is found, it will be the same.
                  assert(cur == NULL);
                  LOG_OUTN(_id,"COMMIT FAILED WorkId=" << _workId << ", guy is on list of candidate of owner " << _ownerId << " to take path\n");
                  break;
                case QEngine::ER_COMMITTED:
                  GECODE_NEVER;
                  break;
                }
              }
              if (goIdle) {
                LOG_OUTN(_id,"GO IDLE\n");
                idle = true;
                m.release();
                // Report that worker is idle
                engine().idle();
              } else {
                m.release();
              }
            } 
          }
        }
        break;
      default:
        GECODE_NEVER;
      }
    }
  }


  /*
   * Perform reset
   *
   */
  void
  QDFS::reset(Space* s) {
    // Grab wait lock for reset
    m_wait_reset.acquire();
    // Release workers for reset
    release(C_RESET);
    // Wait for reset cycle started
    e_reset_ack_start.wait();
    // All workers are marked as busy again
    n_busy = workers();
    for (unsigned int i=1; i<workers(); i++)
      static_cast<QDFS::Worker*>(worker(i))->reset(NULL,0);
    static_cast<QDFS::Worker*>(worker(0))->reset(s,opt().nogoods_limit);
    // Block workers again to ensure invariant
    block();
    // Release reset lock
    m_wait_reset.release();
    // Wait for reset cycle stopped
    e_reset_ack_stop.wait();
  }

  /*
   * Create no-goods
   *
   */
  NoGoods&
  QDFS::nogoods(void) {
    NoGoods* ng;
    // Grab wait lock for reset
    m_wait_reset.acquire();
    // Release workers for reset
    release(C_RESET);
    // Wait for reset cycle started
    e_reset_ack_start.wait();
    ng = &worker(0)->nogoods();
    // Block workers again to ensure invariant
    block();
    // Release reset lock
    m_wait_reset.release();
    // Wait for reset cycle stopped
    e_reset_ack_stop.wait();
    return *ng;
  }

  /*
   * Termination and deletion
   */
  QDFS::~QDFS(void) {
    terminate();
    heap.rfree(_worker);
  }

  /*
   * Termination and deletion
   */
  QDFS::Worker::~Worker(void) {
    qpath.reset(0);
  }
}}}

#endif

// STATISTICS: search-parallel
