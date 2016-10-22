![Quacode logo](/logo_quacode.png)

Quacode
=======

Quacode is a quantified constraint satisfaction problems (QCSP) solver based on Gecode.
Quacode is distributed under the MIT license. For any question or bug report, please contact [Vincent Barichard](http://vincent.barichard.com) ([email](mailto:vincent.barichard@univ-angers.fr).

## QCSP
Quantified Constraint Satisfaction Problems are a generalization of Constraint Satisfaction Problems (CSP) in which variables may be quantified existentially and universally. QCSP offers a natural framework to express problems as finite two-player games or planning under uncertainty.

## Gecode
[Gecode](http://www.gecode.org) is a comprehensive toolkit for developing constraint-based systems. It is well documented and source code is freely available. Quacode is based on Gecode, so you need Gecode to compil and run QCSP examples.

## Free
As Gecode, Quacode is distributed under the MIT licence. Source code and examples are available for download. You may also grab some binaries to quickly have a look at it.

## Publications
- Barichard V., Stephan I., *The cut tool for QCSP*, Accepted at ICTAI 2014.


Download Quacode
================

## From downloadable source and binary archive files
You can grab the last release at the [Github release section](https://github.com/VynceCook/quacode/releases). You will find some archives of the source files and binaries (the binary packages provide the example programs: Baker, NimFibo, MatrixGame, Connect-Four).

## From Gecode web site
Quacode is provided with the last Gecode release (started from version 4.3.1), you can grab the last release from the [Gecode](http://www.gecode.org/) web site.

## Last master sources from Github

You can grab the master version of Quacode:
 
~~~~
git clone https://github.com/VynceCook/quacode.git
~~~~

These sources are the latest ones (not yet released). It may not compile with
the latest Gecode release.

Build Quacode
=============

## From the Gecode source tree
To compile Quacode from the Gecode source tree, you first have to install cmake. To setup the compilation process for your environment, you can launch cmake by invoking:
~~~~
  cmake .
~~~~
in the toplevel Quacode directory.

By default, 'make install' will install all the files in
'/usr/local/bin', '/usr/local/lib' etc.  You can specify
an installation prefix other than '/usr/local' setting the
'CMAKE_INSTALL_PREFIX' option,
for instance `cmake -DCMAKE_INSTALL_PREFIX:PATH=$HOME .`

Then you can compile the code by invoking
~~~~
  make
~~~~
in the toplevel Quacode directory.

After a successful compilation, you can install Quacode
library and examples by invoking
~~~~
  make install
~~~~
in the build directory.

## From the Github sources

