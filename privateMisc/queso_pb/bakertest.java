package queso.experiments;

import gnu.trove.*;
import java.io.*;
import gnu.math.IntNum;

import queso.core.*;
import queso.constraints.*;
import queso.encoding.*;

public class bakertest
{
    public static void main(String[] args)
    {
        System.out.println("Running Baker Test Problem.");
        System.gc();
//        qcsp prob = new qcsp(true, false, true, false, false);
        qcsp prob = new qcsp(true, false, false, false, false);

        mid_domain[] weights=new mid_domain[4];
        weights[0]=new existential(1, 40, prob, "w0");
        weights[1]=new existential(1, 40, prob, "w1");
        weights[2]=new existential(1, 40, prob, "w2");
        weights[3]=new existential(1, 40, prob, "w3");

        if (args.length > 0) {
          int nmax = Integer.parseInt(args[0]);
          mid_domain cst_max=new existential(nmax, nmax, prob, "NMax");
          mid_domain cst_1=new existential(1, 1, prob, "1");
          intnum_bounds [] v0={(intnum_bounds)weights[0], (intnum_bounds) cst_max};
          constraint c0=new comparison_constraint(v0, cst_1, 2, prob);
        }

        mid_domain[] f=new mid_domain[1];
        f[0]=new universal(1, 40, prob, "f");

        mid_domain[] coefs=new mid_domain[4];
        for(int i=0; i<4; i++)
          coefs[i]=new existential(-1, 1, prob, "c"+i);

        mid_domain[] tmp=new mid_domain[4];
        for(int i=0; i<4; i++)
          tmp[i]=new existential(-40, 40, prob, "tmp"+i);

        intnum_bounds [] v0={(intnum_bounds)weights[0], (intnum_bounds)coefs[0], (intnum_bounds) tmp[0]};
        product_constraint c0= new product_constraint(v0, 2, IntNum.one(), IntNum.one(), prob);
        intnum_bounds [] v1={(intnum_bounds)weights[1], (intnum_bounds)coefs[1], (intnum_bounds) tmp[1]};
        product_constraint c1= new product_constraint(v1, 2, IntNum.one(), IntNum.one(), prob);
        intnum_bounds [] v2={(intnum_bounds)weights[2], (intnum_bounds)coefs[2], (intnum_bounds) tmp[2]};
        product_constraint c2= new product_constraint(v2, 2, IntNum.one(), IntNum.one(), prob);
        intnum_bounds [] v3={(intnum_bounds)weights[3], (intnum_bounds)coefs[3], (intnum_bounds) tmp[3]};
        product_constraint c3= new product_constraint(v3, 2, IntNum.one(), IntNum.one(), prob);

        mid_domain[] md={f[0], tmp[0], tmp[1], tmp[2], tmp[3]};
        int [] w1 = {-1, 1, 1, 1, 1};
        constraint c4=new sum_constraint(md, w1, prob);

        //prob.pure_setup_universals();  // Useless on this problem
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
