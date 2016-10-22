Quacode
=======

## Quacode is a quantified constraint satisfaction
problems (QCSP) solver based on Gecode.

## QCSP
Quantified Constraint Satisfaction Problems are a generalization of Constraint Satisfaction Problems (CSP) in which variables may be quantified existentially and universally. QCSP offers a natural framework to express problems as finite two-player games or planning under uncertainty.

## Gecode
[Gecode](http://www.gecode.org) is a comprehensive toolkit for developing constraint-based systems. It is well documented and source code is freely available. Quacode is based on Gecode, so you need Gecode to compil and run QCSP examples.

## Free
As Gecode, Quacode is distributed under the MIT licence. Source code and examples are available for download. You may also grab some binaries to quickly have a look at it.

## Publications

- Barichard V., Stephan I., *The cut tool for QCSP*, Accepted at ICTAI 2014.


Quacode have been developped by Vincent Barichard.
More info is available on http://quacode.barichard.com

To compile Quacode, you have to install cmake. To setup the
compilation process for your environment, you can launch
cmake by invoking
  cmake .
in the toplevel Quacode directory.

By default, 'make install' will install all the files in
'/usr/local/bin', '/usr/local/lib' etc.  You can specify
an installation prefix other than '/usr/local' setting the
'CMAKE_INSTALL_PREFIX' option,
for instance 'cmake -DCMAKE_INSTALL_PREFIX:PATH=$HOME .'

Then you can compile the code by invoking
  make
in the toplevel Quacode directory.

After a successful compilation, you can install Quacode
library and examples by invoking
  make install
in the build directory.
