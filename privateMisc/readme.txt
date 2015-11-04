# -------------------------------------------------
# Génération des versions compilées de Gecode :
# -------------------------------------------------

Pour compiler gecode en debug/static 64 bits:
  ~/Sources/gecode_trunk/configure --enable-static --enable-audit --enable-debug --disable-qt --disable-examples

Pour compiler gecode en release/static 64 bits:
  ~/Sources/gecode_trunk/configure --enable-static --disable-qt --disable-examples

Pour compiler gecode en debug 32 bits
  CC="gcc -m32" CXX="g++ -m32" ~/Sources/gecode_trunk/configure --enable-audit --enable-debug --disable-mpfr

Pour compiler gecode en debug/static 32 bits
  CC="gcc -m32" CXX="g++ -m32" ~/Sources/gecode_trunk/configure --enable-static --enable-audit --enable-debug --disable-qt --disable-examples

Pour compiler gecode en release 32 bits
  CC="gcc -m32" CXX="g++ -m32" ~/Sources/gecode_trunk/configure --disable-qt --disable-examples

Pour compiler gecode en release/static 32 bits
  CC="gcc -m32" CXX="g++ -m32" ~/Sources/gecode_trunk/configure --enable-static --disable-qt --disable-examples

# -------------------------------------------------
# Génération des versions compilées de QuaCode :
# -------------------------------------------------

Pour compiler gecode en debug 32 bits
  CC="gcc -m32" CXX="g++ -m32" cmake -DCMAKE_BUILD_TYPE=Debug -DLOG_AUDIT=ON -DGECODE_BIN=/home/vincent/builds/gecode-debug-32 -DGECODE_SRC=/home/vincent/Sources/gecode_trunk /home/vincent/Sources/quacode

Pour compiler gecode en debug/static 32 bits
  CC="gcc -m32" CXX="g++ -m32" cmake -DSTATIC_LINKING=ON -DCMAKE_BUILD_TYPE=Debug -DLOG_AUDIT=ON -DGECODE_BIN=/home/vincent/builds/gecode-debug-static-32 -DGECODE_SRC=/home/vincent/Sources/gecode_trunk /home/vincent/Sources/quacode/

Pour compiler gecode en release 32 bits
  CC="gcc -m32" CXX="g++ -m32" cmake -DGECODE_BIN=/home/vincent/builds/gecode-release-32 -DGECODE_SRC=/home/vincent/Sources/gecode_trunk /home/vincent/Sources/quacode/

Pour compiler gecode en release/static 32 bits
  CC="gcc -m32" CXX="g++ -m32" cmake -DSTATIC_LINKING=ON -DGECODE_BIN=/home/vincent/builds/gecode-release-static-32 -DGECODE_SRC=/home/vincent/Sources/gecode_trunk /home/vincent/Sources/quacode/

# -------------------------------------------------
# Compilation Quacode
# -------------------------------------------------
cmake -DGECODE_BIN=/home/vincent/builds/gecode-debug -DGECODE_SRC=/home/vincent/Sources/gecode_trunk /home/vincent/Sources/Sources/
cmake -DBUILD_SHARED_LIBS=true -DGECODE_BIN=/home/vincent/builds/gecode-debug -DGECODE_SRC=/home/vincent/Sources/gecode_trunk /home/vincent/Sources/Sources/

# -------------------------------------------------
# Divers
# -------------------------------------------------
I=7; while [ $I -ge 0 ]; do ./qbf -quantifiedConstraints true $I | grep propagations; I=$(($I - 1)); done

Pour générer un core dump : ulimit -c unlimited
Pour killer un programme afin qu'il génère un core dump : kill -11

Pour faire un diff avec le trunk :
diff -urN --exclude='.svn' --exclude='.settings' --exclude='autom4te.cache' -x configure -x Makefile -x config.hpp* ~/Sources/gecode_trunk/ .

svn propset svn:keywords "Author Date Id Revision" all-interval.cpp

# -------------------------------------------------------------------
Ligne de diff entre Quacode du trunk de Gecode et mon répertoire courant.
Ça élimine les lignes de révision du svn et les répertoires 'git' et 'privateMisc'
On peut en faire un patch à appliquer directement sur le trunk de Gecode

diff -uNr -x '.git' -x 'privateMisc' -I '\* *$Date' -I '\* *$Revision' -I '# *$Date' -I '# *$Revision' ~/Sources/gecode_trunk/contribs/quacode/ ./quacode
