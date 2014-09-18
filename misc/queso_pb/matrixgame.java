package queso.experiments;

import gnu.trove.*;
import java.io.*;
import java.util.*;
import gnu.math.IntNum;

import queso.core.*;
import queso.constraints.*;
import queso.encoding.*;

// The parameter "-depth" is the depth of the game.
//        ---> Size of the matrix is 2^depth. Large values may take long to solve...
// The parameter "-file" is the name of instance file to load.
// If a file is given, the depth is ignored
public class matrixgame
{
    public static void main(String[] args)
    {
      System.out.println("Running Matrix Game Problem."); 
      System.gc();
//      qcsp prob = new qcsp(true, false, true, false, false); // Avec affichage instances
      qcsp prob = new qcsp(true, false, false, false, false, false, false); // Sans affichage instances
        
      int depth = 4;// Size of the matrix is 2^depth. Large values may take long to solve...
      boolean bFile = false;
      String fileName = "";
      if (args.length != 0) {
        for (int i=0; i<args.length; i+=2) {
          if (args[i].equals("-depth")) depth = Integer.parseInt(args[i+1]);
          if (args[i].equals("-file")) {
            fileName = args[i+1];
            bFile = true;
          }
        }
      }

      int boardSize = (int) Math.pow(2,depth);

      // If a file is given we take the matrix from the file
      Vector<Integer> tab = new Vector<Integer>();
      if (bFile) {
        try {
          Scanner scanner = new Scanner(new File(fileName));
          while(scanner.hasNextInt()){
             tab.add(scanner.nextInt());
          };
        } catch (FileNotFoundException e) {
          e.printStackTrace();
        };
        boardSize = (int)Math.sqrt(tab.size()); 
        depth = (int) (Math.log(boardSize) / Math.log(2));
      }

      int [] board = new int[boardSize*boardSize];
      for (int i=0; i<boardSize; i++)
        for (int j=0; j<boardSize; j++)
          if (bFile)
            board[j*boardSize+i] = tab.get(j*boardSize+i);
          else
            board[j*boardSize+i] = (int)( Math.random() * 50 ) < 25 ? 0:1;

      System.out.println("   depth: "+depth); 
      // Print initial board
      for (int i=0; i<boardSize; i++)
      {
          for (int j=0; j<boardSize; j++)
            System.out.print(board[i*boardSize+j] + " ");
          System.out.println();
      }
      System.out.println();

      int nbDecisionVar = 2*depth;
      int [] access = new int[nbDecisionVar];
      access[nbDecisionVar-1]=1;
      access[nbDecisionVar-2]=boardSize;
      for (int i=nbDecisionVar-3; i>=0; i--)
        access[i]=access[i+2]*2;
	
      // Defining the player variables
      mid_domain[] x = new mid_domain[nbDecisionVar];
      for (int i=0; i < nbDecisionVar; i++) {
        if ((i%2) == 0) {
          // Existantial Player
          x[i] = new existential(0, 1, prob, "x"+i/2);
        } else {
          // Universal Player
          x[i] = new universal(0, 1, prob, "y"+i/2);
        }
      }
 
      // Make some constants
      mid_domain cstUn=new existential(1, 1, prob, ""+1);
      mid_domain [] cst_board = new mid_domain[boardSize*boardSize];
      for (int i=0; i<boardSize; i++)
        for (int j=0; j<boardSize; j++)
          cst_board[i*boardSize+j] = new existential(board[i*boardSize+j], board[i*boardSize+j], prob, "board_"+i+"_"+j);

      // Goal constaints
      mid_domain boardIdx=new existential(0, boardSize*boardSize, prob, "boardIdx");

      //linear(*this, access, x, IRT_EQ, boardIdx);
      mid_domain[] md0 = new mid_domain[nbDecisionVar+1];
      int [] w0 = new int[nbDecisionVar+1];
      for (int j=0; j<nbDecisionVar; j++) {
        md0[j] = x[j];
        w0[j] = access[j];
      }
      md0[nbDecisionVar] = boardIdx;
      w0[nbDecisionVar] = -1;
      constraint c0=new sum_constraint(md0, w0, prob);

      //element(*this, board, boardIdx, cstUn);
      for (int i=0; i<boardSize; i++)
          for (int j=0; j<boardSize; j++) {
            mid_domain [] vars={cstUn, boardIdx, cst_board[i*boardSize+j]};
            boolean [] neg={false, true, false};
            int [] vals={1, i*boardSize+j, 1};
            or_constraint_values cons1=new or_constraint_values(vars, vals, neg, 0, prob);
          }

      //prob.pure_setup();  
      //prob.pure_setup_universals();  // enables the pure value rule, could take some time to execute
      stopwatch sw= new stopwatch("GMT");
      sw.start();
      prob.establish();
      sw.end();
      System.out.println("Setup time: "+sw.elapsedMillis());
      
      sw= new stopwatch("GMT");
      
      sw.start();
      System.out.println(prob.search());
      sw.end();
      System.out.println("Search time: "+sw.elapsedMillis()+" , Nodes:"+prob.numnodes);
      System.out.println("Propagations: "+prob.propagations);
    }
}
