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

#ifndef __GECODE_SEARCH_PARALLEL_QENGINE_HH__
#define __GECODE_SEARCH_PARALLEL_QENGINE_HH__

#include <gecode/search/parallel/engine.hh>
#include <quacode/qcsp.hh>
#include <quacode/search/parallel/qpath.hh>
#include <quacode/support/dynamic-list.hh>
#include <quacode/support/log.hh>

namespace Gecode { namespace Search { namespace Parallel {

  class QEngine : public Engine {
  public:
    static const unsigned int ER_NONE          = 0;
    static const unsigned int ER_CANCELED      = 1;
    static const unsigned int ER_COMMITTED     = 2;
    static const unsigned int ER_CANDIDATE     = 3;
    static const unsigned int ER_NO_OWNER      = 4;
    static const unsigned int ER_OWNER_CHANGED = 5;

    /// %Parallel quantifed search worker
    class QWorker : public Engine::Worker {
    friend class QPath;
    friend class QEngine;
    public:
      /// \name Data structure for a theft to commit
      struct Work {
        unsigned long int workId;
        bool solution;
      };
    protected:
      /// Current path in search tree
      QPath qpath;
      /// Worker's id
      unsigned int _id;
      /// Worker's owner id
      int _ownerId;
      /// Is the id of the new owner when path is transfered until the worker reset
      int _newOwnerId;
      /// The number of thiefs which have to update their owner
      volatile unsigned int _n_not_updated_owner;
      /// Id of stolen work
      unsigned long int _workId;
      /// Mutex for testing and updating all id (id, _ownerId, _newOwnerId,
      /// _workId, ...)
      Support::Mutex _m_id;
      /// Event for worker to wait until path transfer is done
      Support::Event _e_wait_path;
    public:
      /// Initialize for space \a s with engine \a e
      /// Worker's id is set to \a id
      QWorker(unsigned int id, Space* s, QEngine& e);
      /// Returns id of worker
      unsigned int id(void) const;
      /// Returns owner of worker
      int owner(void) const;
      /// Update owner if needed. Returns true if change has been made
      bool updateOwner(void);
      /// Returns id of stolen work
      unsigned long int work(void) const;

      /// Commit work to owner.
      /// \a solution is true if the thief has found a solution, failed otherwise.
      /// It returns a positive number corresponding to an error, or 0 (ER_NONE)
      /// if succeeded
      unsigned int commit(bool solution);

      /// Check is work has been canceled by owner
      bool workCanceled(void);

      /// Used by thief to ask owner if path transfer needed because
      /// its unfinish work block owner
      bool ownerGivingUpPath(void);
 
      /// Provide access to qengine
      QEngine& engine(void) const;
      /// Destructor
      virtual ~QWorker(void);
    };

  protected:
    /// Array of worker references
    QWorker** _worker;

    /// Mutex for access to work id
    Support::Mutex _m_lastWorkId;
    /// Last id of stolen work
    unsigned long int _lastWorkId;
  public:
    /// Returns next available work id
    unsigned long int newWorkId(void);
    /// Provide access to worker \a i
    QWorker* worker(unsigned int i) const;

    /// \name Engine interface
    //@{
    /// Initialize with options \a o
    QEngine(const Options& o);
    /// Destructor
    virtual ~QEngine(void);
    //@}

    /// For engine to peform thread termination
    void terminate(void);
  };

  /*
   * Engine: initialization
   */
  forceinline
  QEngine::QWorker::QWorker(unsigned int id, Space* s, QEngine& e)
    : Engine::Worker(s,e)
#ifdef LOG_AUDIT    
      , qpath(s == NULL ? 0 : static_cast<int>(e.opt().nogoods_limit),id)
#else
      , qpath(s == NULL ? 0 : static_cast<int>(e.opt().nogoods_limit))
#endif
      , _id(id), _ownerId(-1), _newOwnerId(-1),
      _n_not_updated_owner(0), _workId(0)
  {
    if (s != NULL) _workId = engine().newWorkId();
  }


  forceinline
  QEngine::QEngine(const Options& o) : Engine(o), _lastWorkId(0) { }

  /*
   * Basic access routines
   */
  forceinline QEngine&
  QEngine::QWorker::engine(void) const {
    return static_cast<QEngine&>(_engine);
  }
  forceinline unsigned long int
  QEngine::newWorkId(void) {
    unsigned long int id;
    _m_lastWorkId.acquire();
    _lastWorkId++;
    id = _lastWorkId;
    _m_lastWorkId.release();
    return id;
  }
  forceinline QEngine::QWorker*
  QEngine::worker(unsigned int i) const {
    return static_cast<QWorker*>(_worker[i]);
  }

  /*
   * Engine: statistics
   */
  forceinline unsigned int
  QEngine::QWorker::id(void) const {
    return _id;
  }
  forceinline int
  QEngine::QWorker::owner(void) const {
    return _ownerId;
  }
  forceinline bool
  QEngine::QWorker::updateOwner(void) {
    if (_ownerId == -1) return false;
    int oldOwnerId = _ownerId;
    bool r = false;
    QWorker& WOwner = *engine().worker(_ownerId);
    WOwner._m_id.acquire();
    unsigned int n = 0;
    if ((WOwner._newOwnerId != -1) && (_ownerId != WOwner._newOwnerId)) {
      _m_id.acquire();
      n = --WOwner._n_not_updated_owner;
      _ownerId = WOwner._newOwnerId;
      r = true;
      LOG_OUTN(_id,"UPDATE OWNER to " << _ownerId << ", " << WOwner._n_not_updated_owner << " haven't updated yet\n");
      _m_id.release();
    }
    WOwner._m_id.release();
    // Signal owner that every thiefs have updated their owner
    if (r && (n == 0))
      engine().worker(oldOwnerId)->_e_wait_path.signal();
    return r;
  }

  forceinline unsigned long int
  QEngine::QWorker::work(void) const {
    return _workId;
  }

  forceinline bool
  QEngine::QWorker::ownerGivingUpPath(void) {
    if (_ownerId == -1) return false;
    return engine().worker(_ownerId)->qpath.data().checkCandidate(_workId);
  }

  forceinline bool
  QEngine::QWorker::workCanceled(void) {
    if (_ownerId != -1)
      return engine().worker(_ownerId)->qpath.data().checkCanceled(_workId);
    return false;
  }

  forceinline unsigned int
  QEngine::QWorker::commit(bool solution) {
    unsigned int error = ER_NO_OWNER;
    if (_ownerId != -1) {
      error = ER_NONE;
      QWorker& WOwner = *engine().worker(_ownerId);
      WOwner._m_id.acquire();
      if ((WOwner._newOwnerId == -1) || (_ownerId == WOwner._newOwnerId)) {
        switch (WOwner.qpath.data().commit(_workId,solution)) {
          case QWorkData::ER_CANCELED:
            error = ER_CANCELED;
            break;
          case QWorkData::ER_CANDIDATE:
            error = ER_CANDIDATE;
            break;
          case QWorkData::ER_COMMITTED:
            GECODE_NEVER;
            break;
          case QWorkData::ER_NONE:
            break;
        }
      } else {
        error = ER_OWNER_CHANGED;
      }
      WOwner._m_id.release();
    }
    return error;
  }


  /*
   * Engine: termination control
   */
  forceinline void
  QEngine::terminate(void) {
    // Grab the wait mutex for termination
    _m_wait_terminate.acquire();
    // Release all threads
    release(C_TERMINATE);
    // Be sure that none thread is waiting for a _e_wait_path signal
    for (unsigned int i=0; i < workers(); i++) {
      worker(i)->m.acquire();
      worker(i)->_m_id.acquire();
      if (worker(i)->_n_not_updated_owner != 0) {
        worker(i)->qpath.data().clearCandidate();
        worker(i)->_e_wait_path.signal();
      }
      worker(i)->_m_id.release();
      worker(i)->m.release();
    }
    // Wait until all threads have acknowledged termination request
    _e_term_ack.wait();
    // Release waiting threads
    _m_wait_terminate.release();    
    // Wait until all threads have in fact terminated
    _e_terminate.wait();
    // Now all threads are terminated!
  }

}}}

#endif

// STATISTICS: search-parallel
