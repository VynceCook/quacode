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

#ifndef __GECODE_SEARCH_PARALLEL_QPATH_HH__
#define __GECODE_SEARCH_PARALLEL_QPATH_HH__

#include <quacode/qcsp.hh>
#include <quacode/qspaceinfo.hh>
#include <quacode/search/parallel/qworkdata.hh>

#include <gecode/search.hh>
#include <gecode/search/support.hh>
#include <gecode/search/worker.hh>
#include <gecode/search/meta/nogoods.hh>

namespace Gecode { namespace Search { namespace Parallel {

  /**
   * \brief Depth-first path (stack of edges) supporting recomputation
   */
  class QPath : public NoGoods {
    friend class Search::Meta::NoGoodsProp;
  public:
    /// %Search tree edge for recomputation
    class Edge {
    friend class QPath;
    protected:
      /// Space corresponding to this edge (might be NULL)
      Space* _space;
      /// Current alternative
      unsigned int _alt;
      /// Number of alternatives left
      unsigned int _alt_max;
      /// Array of id of stolen works for this edge
      Support::DynamicList<unsigned long int,Heap> tl;
      /// Choice
      const Choice* _choice;
      /// Quantifier of edge
      TQuantifier _quantifier;
    public:
      /// Default constructor
      Edge(void);
      /// Edge creating with worker \a w for space \a s with 
      /// clone \a c (possibly NULL)
      Edge(Space* s, Space* c);
      
      /// Return space for edge
      Space* space(void) const;
      /// Set space to \a s
      void space(Space* s);
      /// Return quantifier
      TQuantifier quantifier(void) const;
      
      /// Return choice
      const Choice* choice(void) const;
      
      /// Return list of if of stolen work
      Support::DynamicList<unsigned long int,Heap>& stolenWork(void);
      /// Return number for alternatives
      unsigned int alt(void) const;
      /// Return true number for alternatives (excluding lao optimization)
      unsigned int truealt(void) const;
      /// Test whether current alternative is rightmost
      bool rightmost(void) const;
      /// Test whether current alternative was LAO
      bool lao(void) const;
      /// Test whether there is an alternative that can be stolen
      bool work(void) const;
      /// Move to next alternative
      void next(void);
      /// Steal rightmost alternative and return its number
      unsigned int steal(unsigned long int workId);
      
      /// Free memory for edge
      void dispose(QWorkData& data);
    };
  protected:
    /// Stack to store edge information
    Support::DynamicStack<Edge,Heap> ds;
    /// Depth limit for no-good generation
    int _ngdl;
    /// Number of edges that have work for stealing
    unsigned int n_work;
    /// Data about works
    QWorkData _worksData;
  public:
    /// Values for fGivingUp flag
    static const unsigned int F_GIVING_UP_YES  = 0; // No
    static const unsigned int F_GIVING_UP_NO   = 1; // Yes, giving up
    static const unsigned int F_GIVING_UP_WAIT = 2; // Not yet, because candidate have commit their work
  protected:
    /// Set to F_GIVING_UP_YES when path is giving-up
    unsigned int fGivingUp;
  public:
    /// Initialize with no-good depth limit \a l
#ifdef LOG_AUDIT
    QPath(int l, int workerId);
#else
    QPath(int l);
#endif
    /// Return no-good depth limit
    int ngdl(void) const;
    /// Set no-good depth limit to \a l
    void ngdl(int l);
    /// Push space \a c (a clone of \a s or NULL)
    const Choice* push(Worker& stat, Space* s, Space* c);
    /// Generate path for next node with the given quantifier and return whether a next node exists
    bool next(TQuantifier q);
    /// Provide access to topmost edge
    Edge& top(void) const;
    /// Provide access to data stored
    QWorkData& data(void);
    /// Return true if path is giving-up
    unsigned int givingUp(void) const;
    /// Test whether path is empty
    bool empty(void) const;
    /// Return position on stack of last copy
    int lc(void) const;
    /// Unwind the stack up to position \a l (after failure)
    void unwind(int l);
    /// Commit space \a s as described by stack entry at position \a i
    void commit(Space* s, int i) const;
    /// Recompute space according to path 
    Space* recompute(unsigned int& d, unsigned int a_d, Worker& s);
    /// Recompute space according to path
    Space* recompute(unsigned int& d, unsigned int a_d, Worker& s,
                     const Space* best, int& mark);
    /// Return number of entries on stack
    int entries(void) const;
    /// Insert \a path at the start of this stack. It set \a nbThiefs to the
    /// number of thiefs found in path (It passes over workId because it is no 
    /// more longer a thief in the \a path).
    /// It removes workId of the \a path because it is the workId that
    /// previously split the whole path.
    void takePath(unsigned long int workId, unsigned int& nbThiefs, QPath& path);
    /// Reset stack and set no-good depth limit to \a l
    void reset(int l);
    /// Cut edge until \a workId is encountered. \solution is true if thief
    /// has found a solution and false otherwise. Quantifier of edge is stored
    /// in \a quant. Returns true if branches has been cut
    bool cut(unsigned long int workId, bool solution, TQuantifier& quant);
    /// Make a quick check whether stealing might be feasible
    bool steal(void) const;
    /// Worker steal work at depth \a d, stolen work will be labelled \a workId
    Space* steal(Worker& stat, unsigned long int& d, unsigned long int workId);
    /// Post no-goods
    void virtual post(Space& home);
  };

  /*
   * Edge for recomputation
   *
   */
  forceinline
  QPath::Edge::Edge(void) : tl(heap) {}

  forceinline
  QPath::Edge::Edge(Space* s, Space* c)
    : _space(c), _alt(0), tl(heap), _choice(s->choice()) {
    _alt_max = _choice->alternatives()-1;
    const QSpaceInfo* qSpaceInfo = dynamic_cast<const QSpaceInfo*>(s);
    Archive a;
    _choice->archive(a);
    unsigned int brancherId = static_cast<unsigned int>(a[0]);
    _quantifier = qSpaceInfo->brancherQuantifier(brancherId);
  }

  forceinline Space*
  QPath::Edge::space(void) const {
    return _space;
  }
  forceinline void
  QPath::Edge::space(Space* s) {
    _space = s;
  }

  forceinline Support::DynamicList<unsigned long int,Heap>&
  QPath::Edge::stolenWork(void) {
    return tl;
  }
  forceinline unsigned int
  QPath::Edge::alt(void) const {
    return _alt;
  }
  forceinline unsigned int
  QPath::Edge::truealt(void) const {
    assert(_alt < _choice->alternatives());
    return _alt;
  }
  forceinline bool
  QPath::Edge::rightmost(void) const {
    return _alt >= _alt_max;
  }
  forceinline bool
  QPath::Edge::lao(void) const {
    return _alt > _alt_max;
  }
  forceinline bool
  QPath::Edge::work(void) const {
    return _alt < _alt_max;
  }
  forceinline void
  QPath::Edge::next(void) {
    _alt++;
  }
  forceinline unsigned int
  QPath::Edge::steal(unsigned long int workId) {
    assert(work());
    tl.insertFront(workId);
    return _alt_max--;
  }

  forceinline const Choice*
  QPath::Edge::choice(void) const {
    return _choice;
  }

  forceinline TQuantifier
  QPath::Edge::quantifier(void) const {
    return _quantifier;
  }

  forceinline void
  QPath::Edge::dispose(QWorkData& data) {
    if (!tl.empty()) {
      tl.rewind();
      while (!tl.end()) {
        data.cancel(tl());
        tl.removeCurrent();
      }
    }
    delete _space;
    delete _choice;
  }



  /*
   * Depth-first stack with recomputation
   *
   */

#ifdef LOG_AUDIT
  forceinline
  QPath::QPath(int l, int workerId) : ds(heap), _ngdl(l), n_work(0), _worksData(workerId),
  fGivingUp(F_GIVING_UP_NO)
  {}
#else
  forceinline
  QPath::QPath(int l) : ds(heap), _ngdl(l), n_work(0), fGivingUp(F_GIVING_UP_NO)
  {}
#endif

  forceinline int
  QPath::ngdl(void) const {
    return _ngdl;
  }

  forceinline void
  QPath::ngdl(int l) {
    _ngdl = l;
  }

  forceinline const Choice*
  QPath::push(Worker& stat, Space* s, Space* c) {
    if (!ds.empty() && ds.top().lao()) {
      // Topmost stack entry was LAO -> reuse
      ds.pop().dispose(_worksData);
    }
    Edge sn(s,c);
    if (sn.work())
      n_work++;
    ds.push(sn);
    stat.stack_depth(static_cast<unsigned long int>(ds.entries()));
    return sn.choice();
  }

  forceinline bool
  QPath::next(TQuantifier q) {
    fGivingUp = F_GIVING_UP_NO;
    // Generate path for next node with quantifier q and return whether node exists.
    while (!ds.empty())
    {
      if (q != ds.top().quantifier()) {
        if (ds.top().work())
          n_work--;
        ds.pop().dispose(_worksData);
    	} else if (ds.top().rightmost()) {
        if (!ds.top().tl.empty()) {
          // Rightmost alternative reached but guys haven't answer yet
          if (_worksData.addCandidateForPath(ds.top().tl))
            fGivingUp = F_GIVING_UP_YES;
          else
            fGivingUp = F_GIVING_UP_WAIT;
          return false;
        }
        ds.pop().dispose(_worksData);
      } else {
        ds.top().next();
        if (!ds.top().work())
          n_work--;
        return true;
      }
    }
    return false;
  }

  forceinline QPath::Edge&
  QPath::top(void) const {
    assert(!ds.empty());
    return ds.top();
  }

  forceinline QWorkData&
  QPath::data(void) {
    return _worksData;
  }

  forceinline unsigned int
  QPath::givingUp(void) const {
    return fGivingUp;
  }

  forceinline bool
  QPath::empty(void) const {
    return ds.empty();
  }

  forceinline void
  QPath::commit(Space* s, int i) const {
    const Edge& n = ds[i];
    s->commit(*n.choice(),n.alt());
  }

  forceinline int
  QPath::lc(void) const {
    int l = ds.entries()-1;
    while (ds[l].space() == NULL)
      l--;
    return l;
  }

  forceinline int
  QPath::entries(void) const {
    return ds.entries();
  }

  forceinline void
  QPath::unwind(int l) {
    assert((ds[l].space() == NULL) || ds[l].space()->failed());
    int n = ds.entries();
    for (int i=l; i<n; i++) {
      if (ds.top().work())
        n_work--;
      ds.pop().dispose(_worksData);
    }
    assert(ds.entries() == l);
  }

  forceinline void
  QPath::reset(int l) {
    fGivingUp = F_GIVING_UP_NO;
    n_work = 0;
    while (!ds.empty())
      ds.pop().dispose(_worksData);
    _ngdl = l;
  }

  forceinline void
  QPath::takePath(unsigned long int workId, unsigned int& nbThiefs, QPath& path) {
    // workId is the id of work which created this guy path
    // We first remove workId from the top of path because it will
    // no longer record as a thief.
    Support::DynamicList<unsigned long int,Heap> &l = path.top().tl;
    l.rewind();
    while (!l.end() && (l() != workId)) ++l;
    assert(!l.end()); // workId has to be present in list of pending work
    l.removeCurrent();

    nbThiefs = path._worksData.nbCanceledWork();
    int n = path.ds.entries();
#ifdef LOG_AUDIT
    Support::LogObject lo;
    if (nbThiefs > 0)
      lo = lo << "    " << nbThiefs << " were taken from owner canceled list\n";
#endif      

    nbThiefs -= path._worksData.nbCommitWork();
#ifdef LOG_AUDIT
    if (path._worksData.nbCommitWork() > 0)
      lo = lo << "    " << path._worksData.nbCommitWork() << " were discarded because there are in list of commit works of the owner\n";
#endif      
    for ( ; n-- ; ) {
#ifdef LOG_AUDIT
      Support::DynamicList<unsigned long int,Heap> &m = path.ds[n].tl;
      m.rewind();
      while (!m.end()) {
        lo = lo << m() << " ";
        ++m;
      }
#endif      
      nbThiefs += path.ds[n].tl.size();
    }
#ifdef LOG_AUDIT
    LOG_OUTN(_worksData._log_workerId,"    " << nbThiefs << " thiefs were found in path workId=" << lo << "\n");
#endif      

#ifdef LOG_AUDIT
    QPath _path(_ngdl,-1);
#else      
    QPath _path(_ngdl);
#endif      
    while (!ds.empty())
      _path.ds.push(ds.pop());
    while (!path.ds.empty()) {
      Edge e(path.ds.pop());
      _path.ds.push(e);
    }
    while (!_path.ds.empty())
      ds.push(_path.ds.pop());
    n_work += path.n_work;
    path.n_work = 0;

    _worksData.takeCommits(path._worksData);
    _worksData.takeCanceleds(path._worksData);
  }


  forceinline bool
  QPath::cut(unsigned long int workId, bool solution, TQuantifier& quant) {
    TQuantifier _quant;
    bool thiefFound = false;
    int n = ds.entries();
    int l = 0;
    // We ensure that thief is yet in qpath
    for (int i=n; (i--) && !thiefFound; ) {
      Support::DynamicList<unsigned long int, Heap>& tl = ds[i].tl;
      ds[i].tl.rewind();
      while (!tl.end() && (tl() != workId)) ++tl;
      if (!tl.end()) {
        tl.removeCurrent();
        thiefFound = true;
        l = i;
    	  Archive a;
    	  ds[i].choice()->archive(a);
    	  // We assume that the archive method of choice save quantifier of choice at
    	  // last position (a QPosValChoice was used).
    	  if (static_cast<TQuantifier>(a[a.size()-1]) == FORALL)
          _quant = EXISTS;
        else
          _quant = FORALL;
      }
    }

    if (thiefFound &&
        ((solution && (_quant == EXISTS)) || (!solution && (_quant == FORALL)))) {
      // We cut branches
      for (int i=l+1; i<n; i++) {
        if (ds.top().work())
          n_work--;
        ds.pop().dispose(_worksData);
      }
      quant = (solution?FORALL:EXISTS);
      return true;
    }
    return false;
  }

  forceinline bool
  QPath::steal(void) const {
    return n_work > Config::steal_limit;
  }

  forceinline Space*
  QPath::steal(Worker& stat, unsigned long int& d, unsigned long int workId) {
    // Find position to steal: leave sufficient work
    int n = ds.entries()-1;
    unsigned int w = 0;
    while (n >= 0) {
      if (ds[n].work())
        w++;
      if (w > Config::steal_limit) {
#ifdef LOG_AUDIT
        LOG_OUTN(_worksData._log_workerId,"steal limit has been reached n_work=" << n_work << "\n");
#endif
        // Okay, there is sufficient work left
        int l=n;
        // Find last copy
        while (ds[l].space() == NULL)
          l--;
        Space* c = ds[l].space()->clone(false);
        // Recompute, if necessary
        for (int i=l; i<n; i++)
          commit(c,i);
        c->commit(*ds[n].choice(),ds[n].steal(workId));
        if (!ds[n].work())
          n_work--;
        // No no-goods can be extracted above n
        ngdl(std::min(ngdl(),n));
        d = stat.steal_depth(static_cast<unsigned long int>(n+1));
        return c;
      }
      n--;
    }
    return NULL;
  }

  forceinline Space*
  QPath::recompute(unsigned int& d, unsigned int a_d, Worker& stat) {
    assert(!ds.empty());
    // Recompute space according to path
    // Also say distance to copy (d == 0) requires immediate copying

    // Check for LAO
    if ((ds.top().space() != NULL) && ds.top().rightmost()) {
      Space* s = ds.top().space();
      s->commit(*ds.top().choice(),ds.top().alt());
      assert(ds.entries()-1 == lc());
      ds.top().space(NULL);
      // Mark as reusable
      if (ds.entries() > ngdl())
        ds.top().next();
      d = 0;
      return s;
    }
    // General case for recomputation
    int l = lc();             // Position of last clone
    int n = ds.entries();     // Number of stack entries
    // New distance, if no adaptive recomputation
    d = static_cast<unsigned int>(n - l);

    Space* s = ds[l].space()->clone(); // Last clone

    if (d < a_d) {
      // No adaptive recomputation
      for (int i=l; i<n; i++)
        commit(s,i);
    } else {
      int m = l + static_cast<int>(d >> 1); // Middle between copy and top
      int i = l; // To iterate over all entries
      // Recompute up to middle
      for (; i<m; i++ )
        commit(s,i);
      // Skip over all rightmost branches
      for (; (i<n) && ds[i].rightmost(); i++)
        commit(s,i);
      // Is there any point to make a copy?
      if (i<n-1) {
        // Propagate to fixpoint
        SpaceStatus ss = s->status(stat);
        /*
         * Again, the space might already propagate to failure (due to
         * weakly monotonic propagators).
         */
        if (ss == SS_FAILED) {
          // s must be deleted as it is not on the stack
          delete s;
          stat.fail++;
          unwind(i);
          return NULL;
        }
        ds[i].space(s->clone());
        d = static_cast<unsigned int>(n-i);
      }
      // Finally do the remaining commits
      for (; i<n; i++)
        commit(s,i);
    }
    return s;
  }

  forceinline Space*
  QPath::recompute(unsigned int& d, unsigned int a_d, Worker& stat,
                  const Space* best, int& mark) {
    assert(!ds.empty());
    // Recompute space according to path
    // Also say distance to copy (d == 0) requires immediate copying

    // Check for LAO
    if ((ds.top().space() != NULL) && ds.top().rightmost()) {
      Space* s = ds.top().space();
      s->commit(*ds.top().choice(),ds.top().alt());
      assert(ds.entries()-1 == lc());
      if (mark > ds.entries()-1) {
        mark = ds.entries()-1;
        s->constrain(*best);
      }
      ds.top().space(NULL);
      // Mark as reusable
      if (ds.entries() > ngdl())
        ds.top().next();
      d = 0;
      return s;
    }
    // General case for recomputation
    int l = lc();             // Position of last clone
    int n = ds.entries();     // Number of stack entries
    // New distance, if no adaptive recomputation
    d = static_cast<unsigned int>(n - l);

    Space* s = ds[l].space(); // Last clone

    if (l < mark) {
      mark = l;
      s->constrain(*best);
      // The space on the stack could be failed now as an additional
      // constraint might have been added.
      if (s->status(stat) == SS_FAILED) {
        // s does not need deletion as it is on the stack (unwind does this)
        stat.fail++;
        unwind(l);
        return NULL;
      }
      // It is important to replace the space on the stack with the
      // copy: a copy might be much smaller due to flushed caches
      // of propagators
      Space* c = s->clone();
      ds[l].space(c);
    } else {
      s = s->clone();
    }

    if (d < a_d) {
      // No adaptive recomputation
      for (int i=l; i<n; i++)
        commit(s,i);
    } else {
      int m = l + static_cast<int>(d >> 1); // Middle between copy and top
      int i = l;            // To iterate over all entries
      // Recompute up to middle
      for (; i<m; i++ )
        commit(s,i);
      // Skip over all rightmost branches
      for (; (i<n) && ds[i].rightmost(); i++)
        commit(s,i);
      // Is there any point to make a copy?
      if (i<n-1) {
        // Propagate to fixpoint
        SpaceStatus ss = s->status(stat);
        /*
         * Again, the space might already propagate to failure
         *
         * This can be for two reasons:
         *  - constrain is true, so we fail
         *  - the space has weakly monotonic propagators
         */
        if (ss == SS_FAILED) {
          // s must be deleted as it is not on the stack
          delete s;
          stat.fail++;
          unwind(i);
          return NULL;
        }
        ds[i].space(s->clone());
        d = static_cast<unsigned int>(n-i);
      }
      // Finally do the remaining commits
      for (; i<n; i++)
        commit(s,i);
    }
    return s;
  }

}}}

#endif

// STATISTICS: search-parallel
