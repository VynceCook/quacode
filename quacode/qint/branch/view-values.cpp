/* -*- mode: C++; c-basic-offset: 2; indent-tabs-mode: nil -*- */
/*
 *  Main authors:
 *     Vincent Barichard <Vincent.Barichard@univ-angers.fr>
 *
 *  Copyright:
 *     Vincent Barichard, 2013
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

#include <quacode/qcsp.hh>
#include <quacode/qint/branch/view-values.hpp>

namespace Gecode { namespace Int { namespace Branch {

  QPosValuesOrderChoice::QPosValuesOrderChoice(const Brancher& b, const Pos& p, const std::vector<unsigned int>& _parseOrder, IntView x)
    : PosValuesChoice(b,p,x), parseOrder(_parseOrder)
  {
    assert( this->alternatives() == parseOrder.size() );
  }

  QPosValuesOrderChoice::QPosValuesOrderChoice(const Brancher& b, unsigned int a, Pos p,
                                               Archive& e)
    : PosValuesChoice(b,a,p,e) {
    int dpoSize = 0;
    e >> dpoSize;
    parseOrder.resize(dpoSize);
    for (int i=0; i < dpoSize; i++) e >> parseOrder[i];
  }

  size_t
  QPosValuesOrderChoice::size(void) const {
    return PosValuesChoice::size() + sizeof(QPosValuesOrderChoice) - sizeof(PosValuesChoice);
  }

  QPosValuesOrderChoice::~QPosValuesOrderChoice(void) {
  }

  forceinline void
  QPosValuesOrderChoice::archive(Archive& e) const {
    PosValuesChoice::archive(e);
    int dpoSize = (int)parseOrder.size();
    e << dpoSize;
    for (int i=0; i < dpoSize; i++) e << parseOrder[i];
  }

}}}

// STATISTICS: int-branch
