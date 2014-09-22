export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:.

java -Xmx500M -server -cp lib/trove.jar:classes/ queso.experiments.connectfourfullor2 $@
#java -Xmx500M -server -cp lib/trove.jar:classes/ queso.experiments.bakertest $@
#java -Xmx500M -server -cp lib/trove.jar:classes/ queso.experiments.nimfibo $@
#java -Xmx500M -server -cp lib/trove.jar:classes/ queso.experiments.matrixgame $@
