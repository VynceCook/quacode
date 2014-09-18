// J'ai rajouté le paramètre propagations à Qecode, donc il se peut que ça ne compile pas
// sur le Qecode original, il suffit de mettre en commentaire ce qui concerne propagations.
#include "qsolver_qcsp.hh"
#include "QCOPPlus.hh"
#include <iostream>
#include <time.h>

#define UNIVERSAL true
#define EXISTENTIAL false

int main(int argc, char **argv) {
  unsigned long int propagations=0;
  clock_t start, finish;
  start=clock();

  bool qtScope[] = {EXISTENTIAL, UNIVERSAL, EXISTENTIAL};
  int scopeSize[] = {4, 1, 4+4};

  Qcop p(3,qtScope,scopeSize);

  int i=0;
  for (; i<4; i++) p.QIntVar(i,1,40);

  if (argc ==2) {
    int n = atoi(argv[1]);
    rel(*(p.space()), p.var(0), IRT_GR, n);
  }

  IntVarArgs b1;
  for (int j=0; j<4; j++) b1 << p.var(j);
  branch(*(p.space()),b1,INT_VAR_SIZE_MIN(),INT_VAL_MIN());
  p.nextScope();

  p.QIntVar(i,1,40);
  i++;

  IntVarArgs b2;
  for (int j=0; j<5; j++) b2 << p.var(j);
  branch(*(p.space()),b2,INT_VAR_SIZE_MIN(),INT_VAL_MIN());
  p.nextScope();

  for (; i<9; i++) p.QIntVar(i,-1,1);
  for (; i<13; i++) p.QIntVar(i,-40,40);

  rel(*(p.space()), p.var(0) * p.var(5) == p.var(9));
  rel(*(p.space()), p.var(1) * p.var(6) == p.var(10));
  rel(*(p.space()), p.var(2) * p.var(7) == p.var(11));
  rel(*(p.space()), p.var(3) * p.var(8) == p.var(12));
  rel(*(p.space()), p.var(9) + p.var(10) + p.var(11) + p.var(12) == p.var(4));

  IntVarArgs b3;
  for (int j=0; j<13; j++) b3 << p.var(j);
  branch(*(p.space()),b3,INT_VAR_SIZE_MIN(),INT_VAL_MIN());
  p.nextScope();

//  p.makeStructure(propagations);
  p.makeStructure();
  QCSP_Solver s(&p);

  unsigned long int nodes=0;

//  Strategy outcome  = s.solve(nodes,propagations);
  Strategy outcome  = s.solve(nodes);
  finish=clock();
  cout << "  outcome: " << ( outcome.isFalse() ? "FALSE" : "TRUE") << endl;
  cout << "  nodes visited: " << nodes << endl;
//  cout << "  propagations: " << propagations << endl;
  cout << "Time taken: " << (finish-start) << " microseconds."<<endl;

  return 0;
}
