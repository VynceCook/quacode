package queso.experiments;

import gnu.trove.*;
import java.io.*;
import gnu.math.IntNum;

import queso.core.*;
import queso.constraints.*;
import queso.encoding.*;

public class nimfibo
{
    public static void main(String[] args)
    {
      System.out.println("Running NimFibo Problem.");
      System.gc();
//      qcsp prob = new qcsp(true, false, true, false, false); // Avec affichage instances
      qcsp prob = new qcsp(true, false, false, false, false, false, false); // Sans affichage instances

      int NMatchs = 4;
      if (args.length > 0) NMatchs = Integer.parseInt(args[0]);
      System.out.println("  Searching solution for "+NMatchs+" matchs.");
      int nIterations = ((NMatchs%2)==1)?NMatchs:NMatchs+1;

      // Create problem decision variables
      mid_domain[] x = new mid_domain[nIterations];
      x[0] = new existential(1, NMatchs-1, prob, "x0");
      for (int i=1; i < nIterations; i++) {
        if ((i%2) == 1) {
          // Universal Player
          x[i] = new universal(1, NMatchs-1, prob, "y"+i);
        } else {
          // Existantial Player
          x[i] = new existential(1, NMatchs-1, prob, "x"+i);
        }
      }

      // Make some constants
      mid_domain cst_NMatchs=new existential(NMatchs, NMatchs, prob, "NMatchs");
      int numconstants = 3;
      mid_domain [] constants= new mid_domain[numconstants];
      for(int i=0; i<numconstants; i++) constants[i]=new existential(i, i, prob, ""+i);

      // Post constraint constraints
      mid_domain oim1 = new existential(1, 1, prob, "o_im1");
      for (int i=1; i < nIterations; i++) {
        mid_domain o1 = new existential(0, 1, prob, "o1_"+i);
        mid_domain o2 = new existential(0, 1, prob, "o2_"+i);
        mid_domain oi = new existential(0, 1, prob, "oi_"+i);

        //rel(*this, QX[i][0], IRT_LQ, expr(*this, 2*QX[i-1][0]), o1);
        mid_domain tmp = new existential(1, 2*(NMatchs-1), prob, "tmp_"+i);
        intnum_bounds [] v0={(intnum_bounds)x[i-1], (intnum_bounds) constants[2], (intnum_bounds) tmp};
        product_constraint c0= new product_constraint(v0, 2, IntNum.one(), IntNum.one(), prob);
        intnum_bounds [] v1={(intnum_bounds)x[i], (intnum_bounds) tmp};
        constraint c1=new comparison_constraint(v1, o1, -1, prob);

        //linear(*this, x, IRT_LQ, NMatchs, o2);
        mid_domain[] md2 = new mid_domain[i+2];
        mid_domain tmp2 = new existential(1, (i+1)*NMatchs, prob, "tmp2_"+i);
        int [] w2 = new int[i+2];
        for (int j=0; j<=i; j++) {
          md2[j] = x[j];
          w2[j] = 1;
        }
        md2[i+1] = tmp2;
        w2[i+1] = -1;
        constraint c2=new sum_constraint(md2, w2, prob);
        intnum_bounds [] v3={(intnum_bounds)tmp2, (intnum_bounds) cst_NMatchs};
        constraint c3=new comparison_constraint(v3, o2, -1, prob);

        if ((i%2) == 1) {
          // Universal Player
          //rel(*this, o_im1 == ((o1 && o2) >> oi));  !o1 || !o2 || oi
          mid_domain [] v4={o1, o2, oi, oim1};
          boolean [] neg4={true, true, false, false};
          constraint c4 = new or_constraint(v4, neg4, 3, prob);
        } else {
          // Existantial Player
          //rel(*this, o_im1 == (o1 && o2 && oi));  !( !o1 || !o2 || !oi )
          mid_domain [] v4={o1, o2, oi, oim1};
          boolean [] neg4={true, true, true, true};
          constraint c4 = new or_constraint(v4, neg4, 3, prob);
        }
        oim1 = oi;
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
