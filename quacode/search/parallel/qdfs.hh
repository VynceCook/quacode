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

#ifndef __GECODE_SEARCH_PARALLEL_QDFS_HH__
#define __GECODE_SEARCH_PARALLEL_QDFS_HH__

#include <quacode/search/parallel/qengine.hh>

namespace Gecode { namespace Search { namespace Parallel {

  /// %Parallel depth-first search engine
  class QDFS : public QEngine {
  protected:
    /// %Parallel depth-first quantifed search worker
    class Worker : public QEngine::QWorker {
    public:
      /// Initialize worker \a id for space \a s with engine \a e
      Worker(unsigned int id, Space* s, QDFS& e);
      /// Provide access to engine
      QDFS& engine(void) const;
      /// Hand over some work (NULL if no work available), it set workId if work has been handed
      Space* steal(unsigned long int& d, unsigned long int& workId);
      /// Return statistics
      Statistics statistics(void);
      /// Start execution of worker
      virtual void run(void);
      /// Try to find some work
      void find(void);
      /// Reset worker to restart at space \a s
      void reset(Space* s, int ngdl);
      /// Destructor
      virtual ~Worker(void);
    };
  public:
    /// \name Search control
    //@{
    /// Report solution \a s
    void solution(Space* s);
    //@}

    /// \name Engine interface
    //@{
    /// Initialize for space \a s with options \a o
    QDFS(Space* s, const Options& o);
    /// Return statistics
    virtual Statistics statistics(void) const;
    /// Reset engine to restart at space \a s
    virtual void reset(Space* s);
    /// Return no-goods
    virtual NoGoods& nogoods(void);
    /// Destructor
    virtual ~QDFS(void);
    //@}
  };


  /*
   * Basic access routines
   */
  forceinline QDFS&
  QDFS::Worker::engine(void) const {
    return static_cast<QDFS&>(_engine);
  }

  /*
   * Engine: initialization
   */
  forceinline
  QDFS::Worker::Worker(unsigned int id, Space* s, QDFS& e)
    : QWorker(id,s,e)
  {}
  forceinline
  QDFS::QDFS(Space* s, const Options& o)
    : QEngine(o) {
    // Create workers
    _worker = static_cast<QWorker**>
      (heap.ralloc(workers() * sizeof(Worker*)));
    // The first worker gets the entire search tree
    _worker[0] = new Worker(0,s,*this);
    // All other workers start with no work
    for (unsigned int i=1; i<workers(); i++)
      _worker[i] = new Worker(i,NULL,*this);
    // Block all workers
    block();
    // Create and start threads
    for (unsigned int i=0; i<workers(); i++)
      Support::Thread::run(_worker[i]);
  }

  /*
   * Statistics
   */
  forceinline Statistics
  QDFS::Worker::statistics(void) {
    m.acquire();
    Statistics s = *this;
    m.release();
    return s;
  }

  forceinline void
  QDFS::Worker::reset(Space* s, int ngdl) {
    delete cur;
    qpath.reset((s != NULL) ? ngdl : 0);
    _m_id.acquire();
    _ownerId = -1;
    _newOwnerId = -1;
    _workId = 0;
    _m_id.release();
    d = 0;
    idle = false;
    if ((s == NULL) || (s->status(*this) == SS_FAILED)) {
      delete s;
      cur = NULL;
    } else {
      cur = s;
    }
    Search::Worker::reset();
  }

  /*
   * Worker: finding and stealing working
   */
  forceinline Space*
  QDFS::Worker::steal(unsigned long int& d, unsigned long int& workId) {
    /*
     * Make a quick check whether the worker might have work
     *
     * If that is not true any longer, the worker will be asked
     * again eventually.
     */
    if ((qpath.givingUp() != QPath::F_GIVING_UP_NO) || !qpath.steal())
      return NULL;

    m.acquire();
    workId = engine().newWorkId();
    Space* s = qpath.steal(*this,d,workId);
    m.release();
    // Tell that there will be one more busy worker
    if (s != NULL) 
      engine().busy();
    return s;
  }



  /*
   * Engine: search control
   */
  forceinline void 
  QDFS::solution(Space* s) {
    m_search.acquire();
    bool bs = signal();
    solutions.push(s);
    if (bs)
      e_search.signal();
    m_search.release();
  }
  

  /*
   * Worker: finding and stealing working
   */
  forceinline void
  QDFS::Worker::find(void) {
    // Try to find new work (even if there is none)
    for (int i=engine().workers()-1; i>=0; i--) {
      unsigned long int r_d = 0ul;
      unsigned long int workId = 0;
      if (Space* s = static_cast<QDFS::Worker*>(engine().worker(i))->steal(r_d,workId)) {
        // Reset this guy
        m.acquire();
        _workId = workId;
        _ownerId = i;
        LOG_OUTN(_id,"STEAL WorkId=" << _workId << " from " << _ownerId << "\n");
        idle = false;
        // Not idle but also does not have the root of the tree
        path.ngdl(0);
        d = 0;
        cur = s;
        Search::Worker::reset(r_d);
        m.release();
        return;
      }
    }
  }

}}}

#endif

// STATISTICS: search-parallel
