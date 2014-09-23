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

#ifndef __GECODE_SEARCH_PARALLEL_QWORKDATA_HH__
#define __GECODE_SEARCH_PARALLEL_QWORKDATA_HH__

#include <gecode/support.hh>
#include <quacode/support/log.hh>
#include <quacode/support/dynamic-list.hh>

namespace Gecode { namespace Search { namespace Parallel {

  /**
   * \brief Data about works of path used in workers
   */
  class QWorkData {
  public:
    static const unsigned int ER_NONE      = 0;
    static const unsigned int ER_CANCELED  = 1;
    static const unsigned int ER_COMMITTED = 2;
    static const unsigned int ER_CANDIDATE = 3;
    /// \name Data structure for a work to commit
    struct Work {
      unsigned long int workId;
      bool solution;
    };
  protected:
    /// Ids of work to commit
    Support::DynamicList<Work,Heap> _workToCommit;
    /// Ids of works cut (corresponding to worker/thief to reset)
    Support::DynamicList<unsigned long int,Heap> _workCanceled;
    /// Ids of works which cant take path of this guy 
    Support::DynamicList<unsigned long int,Heap> _workToGivePath;

    /// Mutex for access to these
    mutable Support::Mutex _m_data;

    /// Check if work is waiting for commit. If \a removeIfFound is true,
    /// the work will be removed from the _workToCommit list
    bool isWaitingForCommit(unsigned long int workId, bool removeIfFound = false);
    /// Check if work has been canceled. If \a removeIfFound is true,
    /// the work will be removed from the _workCanceled list
    bool isCanceled(unsigned long int workId, bool removeIfFound = false);
    /// Check if work is candidate for taking path. If \a removeIfFound is true,
    /// the work will be removed from the _workToGivePath list
    bool isCandidateForPath(unsigned long int workId, bool removeIfFound = false);
  public:
#ifdef LOG_AUDIT    
    /// Id of worker which owns these data usefull for logging
    int _log_workerId;
    /// Initialize
    QWorkData(int workerId);
#else
    /// Initialize
    QWorkData(void);
#endif

    // Returns number of works to commit
    unsigned nbCommitWork(void) const;
    // Returns number of canceled works
    unsigned int nbCanceledWork(void) const;
    // Returns number of works waiting for taking path
    unsigned int nbWorkWaitingPath(void) const;

    /// Add a new work to the pendings works to commit. Returns error code
    /// if commit failed
    unsigned int commit(unsigned long int workId, bool solution);
    ///  Take all pending works to commit of \a wd and inserts them in our list
    /// of pending works to commit. \a wd is reset
    void takeCommits(QWorkData& wd);
    /// Set \a wo to next pending work. Returns true if found. Work is
    /// removed from the list of pending works
    bool getCommitWork(QWorkData::Work& wo);
    /// Clear the list of pending works to commit
    void clearCommitWork(void);

    /// Add a new work to the canceled works. Returns true if workId
    /// has been added to canceled works
    bool addCandidateForPath(unsigned long int workId);
    /// Check if \a workId is candidate for path. Returns true if found.
    /// If found, the list of candidate is cleared.
    bool checkCandidate(unsigned long int workId);
    /// Add a new works to the candidate for taking path. Returns true if all
    /// work ids have been added to the list of candidates. If one of the
    /// candidates can't be added, none of the list will be added.
    /// The list \a lWorkId is not changed. Be carefull, \a lWorkId must
    /// not be empty
    bool addCandidateForPath(Support::DynamicList<unsigned long int,Heap>& lWorkId);
    /// Clear the list of candidates
    void clearCandidate(void);

    /// Add a new work to the candidate for taking path. Returns true if workId
    /// has been added to the list of candidates
    bool cancel(unsigned long int workId);
    ///  Take all canceled works of \a wd and inserts them in our list
    /// of canceled works. \a wd is reset
    void takeCanceleds(QWorkData& wd);
    /// Check if \a workId has been canceled. Returns true if found. Work is
    /// removed from the list of pending works
    bool checkCanceled(unsigned long int workId);
  };

  /*
   * Initialization
   */
#ifdef LOG_AUDIT
  forceinline
  QWorkData::QWorkData(int workerId)
    : _workToCommit(heap), _workCanceled(heap), _workToGivePath(heap),
      _log_workerId(workerId) {}
#else
  forceinline
  QWorkData::QWorkData(void)
    : _workToCommit(heap), _workCanceled(heap), _workToGivePath(heap) {}
#endif

  forceinline
  bool QWorkData::isWaitingForCommit(unsigned long int workId, bool removeIfFound) {
    if (!_workToCommit.empty()) {
      _workToCommit.rewind();
      while (!_workToCommit.end() && (_workToCommit().workId != workId)) ++_workToCommit;
      if (!_workToCommit.end()) {
        if (removeIfFound) {
          _workToCommit.removeCurrent();
        }
        return true;
      }
    }
    return false;
  }

  forceinline
  bool QWorkData::isCanceled(unsigned long int workId, bool removeIfFound) {
    if (!_workCanceled.empty()) {
      _workCanceled.rewind();
      while (!_workCanceled.end() && (_workCanceled() != workId)) ++_workCanceled;
      if (!_workCanceled.end()) {
        if (removeIfFound) {
          _workCanceled.removeCurrent();
        }
        return true;
      }
    }
    return false;
  }

  forceinline
  bool QWorkData::isCandidateForPath(unsigned long int workId, bool removeIfFound) {
    if (!_workToGivePath.empty()) {
      _workToGivePath.rewind();
      while (!_workToGivePath.end() && (_workToGivePath() != workId)) ++_workToGivePath;
      if (!_workToGivePath.end()) {
        if (removeIfFound) {
          _workToGivePath.removeCurrent();
        }
        return true;
      }
    }
    return false;
  }

  forceinline
  unsigned int QWorkData::nbCommitWork(void) const {
    unsigned int n = 0;
    _m_data.acquire();
    n = _workToCommit.size();
    _m_data.release();
    return n;
  }
  forceinline
  unsigned int QWorkData::nbCanceledWork(void) const {
    unsigned int n = 0;
    _m_data.acquire();
    n = _workCanceled.size();
    _m_data.release();
    return n;
  }
  forceinline
  unsigned int QWorkData::nbWorkWaitingPath(void) const {
    unsigned int n = 0;
    _m_data.acquire();
    n = _workToGivePath.size();
    _m_data.release();
    return n;
  }

  /*
   * Work data control
   */
  forceinline unsigned int
  QWorkData::commit(unsigned long int workId, bool solution) {
    unsigned int error = ER_NONE;
    _m_data.acquire();
    assert(!isWaitingForCommit(workId));
    if (isCanceled(workId,true)) {
      error = ER_CANCELED;
#ifdef LOG_AUDIT
      LOG_OUTN(_log_workerId,"COMMIT WorkId=" << workId << " solution=" << solution << " CANCELED BECAUSE WORK CANCELED\n");
#endif
    } else if (isCandidateForPath(workId)) {
      error = ER_CANDIDATE;
#ifdef LOG_AUDIT
      LOG_OUTN(_log_workerId,"COMMIT WorkId=" << workId << " solution=" << solution << " CANCELED BECAUSE CANDIDATE TO GIVE PATH\n");
#endif
    } else {
#ifdef LOG_AUDIT
      LOG_OUTN(_log_workerId,"COMMIT WorkId=" << workId << " solution=" << solution << "\n");
#endif    
      QWorkData::Work t;
      t.workId = workId;
      t.solution = solution;
      _workToCommit.insertFront(t);
    }
    _m_data.release();
    return error;
  }

  forceinline void
  QWorkData::takeCommits(QWorkData& wd) {
    _m_data.acquire();
    wd._m_data.acquire();
    wd._workToCommit.rewind();
    while (!wd._workToCommit.end()) {
      assert(!isCanceled(wd._workToCommit().workId));
      assert(!isCandidateForPath(wd._workToCommit().workId));
      assert(!isWaitingForCommit(wd._workToCommit().workId));
#ifdef LOG_AUDIT
      LOG_OUTN(_log_workerId,"COMMIT WorkId=" << wd._workToCommit().workId << " TRANSFERED from worker " << wd._log_workerId << "\n");
#endif
      _workToCommit.insertFront(wd._workToCommit());
      wd._workToCommit.removeCurrent();
    }
    wd._m_data.release();
    _m_data.release();
  }


  forceinline
  bool QWorkData::getCommitWork(QWorkData::Work& wo) {
    bool r = false;
    _m_data.acquire();
    if (!_workToCommit.empty()) {
      _workToCommit.rewind();
      wo = _workToCommit();
      _workToCommit.removeCurrent();
#ifdef LOG_AUDIT
      LOG_OUTN(_log_workerId,"COMMITED WorkId=" << wo.workId << "\n");
#endif    
      r = true;
    }
    _m_data.release();
    return r;
  }

  forceinline
  void QWorkData::clearCommitWork(void) {
    _m_data.acquire();
    _workToCommit.reset();
    _m_data.release();
  }

  forceinline bool
  QWorkData::cancel(unsigned long int workId) {
    bool r = false;
    _m_data.acquire();
    assert(!isCanceled(workId));
    assert(!isCandidateForPath(workId));
    if (!isWaitingForCommit(workId,true)) {
#ifdef LOG_AUDIT
      LOG_OUTN(_log_workerId,"CANCEL WorkId=" << workId << "\n");
#endif    
      _workCanceled.insertFront(workId);
      r = true;
    } else {
#ifdef LOG_AUDIT
      LOG_OUTN(_log_workerId,"CANCEL WorkId=" << workId << " CANCELED BECAUSE PENDING TO COMMIT WORK\n");
#endif
    }
    _m_data.release();
    return r;
  }

  forceinline void
  QWorkData::takeCanceleds(QWorkData& wd) {
    _m_data.acquire();
    wd._m_data.acquire();
    wd._workCanceled.rewind();
    while (!wd._workCanceled.end()) {
      assert(!isCanceled(wd._workCanceled()));
      assert(!isCandidateForPath(wd._workCanceled()));
      assert(!isWaitingForCommit(wd._workCanceled()));
#ifdef LOG_AUDIT
      LOG_OUTN(_log_workerId,"CANCEL WorkId=" << wd._workCanceled() << " TRANSFERED from worker " << wd._log_workerId << "\n");
#endif
      _workCanceled.insertFront(wd._workCanceled());
      wd._workCanceled.removeCurrent();
    }
    wd._m_data.release();
    _m_data.release();
  }

  forceinline
  bool QWorkData::checkCanceled(unsigned long int workId) {
    bool r = false;
    _m_data.acquire();
    if (isCanceled(workId,true)) {
#ifdef LOG_AUDIT
      LOG_OUTN(_log_workerId,"CANCELED WorkId=" << workId << "\n");
#endif    
      r = true;
    }
    _m_data.release();
    return r;
  }

  forceinline bool
  QWorkData::addCandidateForPath(unsigned long int workId) {
    bool r = false;
    _m_data.acquire();
    assert(!isCanceled(workId));
    assert(!isCandidateForPath(workId));
    if (!isWaitingForCommit(workId)) {
#ifdef LOG_AUDIT
      LOG_OUTN(_log_workerId,"GIVING-UP PATH (start)\n");
      LOG_OUTN(_log_workerId,"      CANDIDATE WorkId=" << workId << " ADDED\n");
#endif    
      _workToGivePath.insertFront(workId);
      r = true;
    } else {
#ifdef LOG_AUDIT
      LOG_OUTN(_log_workerId,"      CANDIDATE WorkId=" << workId << " NOT ADDED\n");
#endif
    }
    _m_data.release();
    return r;
  }

  forceinline bool
  QWorkData::addCandidateForPath(Support::DynamicList<unsigned long int,Heap>& lWorkId) {
    bool r = true;
    _m_data.acquire();
    assert(_workToGivePath.empty());
    lWorkId.rewind();
    while (!lWorkId.end() && r) {
      if (!isWaitingForCommit(lWorkId())) {
        _workToGivePath.insertFront(lWorkId());
      } else {
        r = false;
        _workToGivePath.reset();
      }
      ++lWorkId;
    }
#ifdef LOG_AUDIT
    Support::LogObject lo;
    lo = lo << "GIVING-UP PATH (start)\n";
    lo = lo << "      CANDIDATE WorkIds=";
    lWorkId.rewind();
    while (!lWorkId.end()) {
      lo = lo << lWorkId() << " ";
      ++lWorkId;
    }
#endif
    if (r) {
      assert(!_workToGivePath.empty());
#ifdef LOG_AUDIT
      lo = lo << " ADDED\n";
      LOG_OUTN(_log_workerId,lo);
#endif
    } else {
#ifdef LOG_AUDIT
      lo = lo << " NOT ADDED\n";
      LOG_OUTN(_log_workerId,lo);
#endif
    }
    _m_data.release();
    return r;
  }
 
  forceinline
  bool QWorkData::checkCandidate(unsigned long int workId) {
    bool r = false;
    _m_data.acquire();
    if (isCandidateForPath(workId)) {
#ifdef LOG_AUDIT
      LOG_OUTN(_log_workerId,"CANDIDATE FOUND WorkId=" << workId << "\n");
#endif
      _workToGivePath.reset();
      r = true;
    }
    _m_data.release();
    return r;
  }

  forceinline
  void QWorkData::clearCandidate(void) {
    _m_data.acquire();
    _workToGivePath.reset();
    _m_data.release();
  }

}}}

#endif

// STATISTICS: search-parallel
