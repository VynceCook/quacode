Pour compiler gecode en 32 bits
CC="gcc -m32" CXX="g++ -m32" ~/Dropbox/gecode_trunk/configure --enable-audit --enable-debug --disable-mpfr

cmake -DGECODE_BIN=/home/vincent/builds/gecode-debug -DGECODE_SRC=/home/vincent/Dropbox/gecode_trunk /home/vincent/Dropbox/Sources/
cmake -DBUILD_SHARED_LIBS=true -DGECODE_BIN=/home/vincent/builds/gecode-debug -DGECODE_SRC=/home/vincent/Dropbox/gecode_trunk /home/vincent/Dropbox/Sources/

I=7; while [ $I -ge 0 ]; do ./qbf -quantifiedConstraints true $I | grep propagations; I=$(($I - 1)); done

Pour générer un core dump : ulimit -c unlimited
Pour killer un programme afin qu'il génère un core dump : kill -11

Pour faire un diff avec le trunk :
diff -urN --exclude='.svn' --exclude='.settings' --exclude='autom4te.cache' -x configure -x Makefile -x config.hpp* ~/Dropbox/gecode_trunk/ .

svn propset svn:keywords "Author Date Id Revision" all-interval.cpp

