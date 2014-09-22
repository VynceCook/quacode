package queso.experiments;

import gnu.trove.*;
import java.io.*;
import java.util.*;

import queso.core.*;
import queso.constraints.*;
import queso.encoding.*;

class c4 extends qcsp
{
    c4(boolean bPrint, int rows, int cols)
    {
       super(true, false, true, false, false);
       assert(bPrint == true);

      this.rows=rows;
      this.cols=cols;
    }
    c4(int rows, int cols)
    {
        super(true, false, false, false, false);
        this.rows=rows;
        this.cols=cols;
    }
    final int rows;
    final int cols;

    public void printsol()
    {
        System.out.println("True: Board after last move:");
        for(int row=rows-1; row>=0; row--)
        {
            for(int col=0; col<cols; col++)
            {
                String st="board["+(rows*cols-1)+"]["+col+"]["+row+"]";
                System.out.print(findval(st)+"  ");
            }
            System.out.println();
        }

        System.out.println("Moves:");
        for(int move=0; move<rows*cols; move++)
            System.out.println(move+" : "+findval("move"+move));

        //verifysol();
    }

    int findval(String varname) // given a variable name, return the value.
    {
        for(variable t : variables)
        {
            if(t.toString().equals(varname))
            {
                mid_domain t2=(mid_domain)t;
                for(int j=t2.lowerbound(); j<=t2.upperbound(); j++)
                {
                    if(t2.is_present(j))
                    {
                        return j;
                    }
                }
                assert false: "No value found for variable "+varname;
            }
        }
        assert false: "No variable found:"+varname;
        return -1;
    }

    void verifysol()
    {
        int [] board=new int[9];

        for(int move=0; move<9; move++)
        {
            int themove=findval("move"+move);
            assert board[move]==0;
            if(move%2==0) board[move]=10; else board[move]=11;
            int temp=wins(board);
            if(temp==0)
            {
                assert false: "Claimed true is not a true.";
            }
            else if(temp==1)
            {
                return;
            }
        }
        assert false: "Claimed true is not a true.";
    }

    int wins(int []board)
    {
        // player 10 lost == 0, wins ==1 and undetermined==2
        if( (board[0]==10 && board[1]==10 && board[2]==10)
        || (board[3]==10 && board[4]==10 && board[5]==10)
        || (board[6]==10 && board[7]==10 && board[8]==10)

        // verticals
        || (board[0]==10 && board[3]==10 && board[6]==10)
        || (board[1]==10 && board[4]==10 && board[7]==10)
        || (board[2]==10 && board[5]==10 && board[8]==10)

        // diagonals
        || (board[0]==10 && board[4]==10 && board[8]==10)
        || (board[2]==10 && board[4]==10 && board[6]==10) )
        {
            return 1;
        }

        if( (board[0]==11 && board[1]==11 && board[2]==11)
        || (board[3]==11 && board[4]==11 && board[5]==11)
        || (board[6]==11 && board[7]==11 && board[8]==11)

        // verticals
        || (board[0]==11 && board[3]==11 && board[6]==11)
        || (board[1]==11 && board[4]==11 && board[7]==11)
        || (board[2]==11 && board[5]==11 && board[8]==11)

        // diagonals
        || (board[0]==11 && board[4]==11 && board[8]==11)
        || (board[2]==11 && board[4]==11 && board[6]==11) )
        {
            return 0;
        }
        // it's indeterminate

        return 2;
    }
}

class connectfourfullor
{
    public static void main(String[] args)
    {
        connectfourfull.runit(4,4,4, Ctype.disjunction, Ctype.disjunction,
            Ctype.disjunction, Ctype.disjunction, Ctype.disjunction,
            Ctype.disjunction, Ctype.disjunction);

        /*connectfourfull.runit(4,5,4, Ctype.disjunction, Ctype.sqgac,
            Ctype.sqgac, Ctype.sqgac, Ctype.sqgac,
            Ctype.sqgac, Ctype.sqgac);
        connectfourfull.runit(4,5,4, Ctype.disjunction, Ctype.sqgac,
            Ctype.sqgac, Ctype.sqgac, Ctype.sqgac,
            Ctype.sqgac, Ctype.sqgac);*/

        System.out.println("Assuming warmed up from here");

        /*connectfourfull.runit(4,5,4, Ctype.disjunction, Ctype.sqgac,
            Ctype.sqgac, Ctype.sqgac, Ctype.sqgac,
            Ctype.sqgac, Ctype.sqgac);
        connectfourfull.runit(4,5,4, Ctype.sqgac, Ctype.disjunction,
            Ctype.sqgac, Ctype.sqgac, Ctype.sqgac,
            Ctype.sqgac, Ctype.sqgac);
        connectfourfull.runit(4,5,4, Ctype.sqgac, Ctype.sqgac,
            Ctype.disjunction, Ctype.sqgac, Ctype.sqgac,
            Ctype.sqgac, Ctype.sqgac);
        connectfourfull.runit(4,5,4, Ctype.sqgac, Ctype.sqgac,
            Ctype.sqgac, Ctype.disjunction, Ctype.sqgac,
            Ctype.sqgac, Ctype.sqgac);
        connectfourfull.runit(4,5,4, Ctype.sqgac, Ctype.sqgac,
            Ctype.sqgac, Ctype.sqgac, Ctype.disjunction,
            Ctype.sqgac, Ctype.sqgac);
        connectfourfull.runit(4,5,4, Ctype.sqgac, Ctype.sqgac,
            Ctype.sqgac, Ctype.sqgac, Ctype.sqgac,
            Ctype.disjunction, Ctype.sqgac);
        connectfourfull.runit(4,5,4, Ctype.sqgac, Ctype.sqgac,
            Ctype.sqgac, Ctype.sqgac, Ctype.sqgac,
            Ctype.sqgac, Ctype.disjunction);*/

        // different size boards with disjunction model.

        /*connectfourfull.runit(4,4,4, Ctype.disjunction, Ctype.disjunction,
            Ctype.disjunction, Ctype.disjunction, Ctype.disjunction,
            Ctype.disjunction, Ctype.disjunction);*/

        connectfourfull.runit(4,4,5, Ctype.disjunction, Ctype.disjunction,
            Ctype.disjunction, Ctype.disjunction, Ctype.disjunction,
            Ctype.disjunction, Ctype.disjunction);

        connectfourfull.runit(4,5,4, Ctype.disjunction, Ctype.disjunction,
            Ctype.disjunction, Ctype.disjunction, Ctype.disjunction,
            Ctype.disjunction, Ctype.disjunction);
        /*
        connectfourfull.runit(4,5,5, Ctype.disjunction, Ctype.disjunction,
            Ctype.disjunction, Ctype.disjunction, Ctype.disjunction,
            Ctype.disjunction, Ctype.disjunction);

        connectfourfull.runit(4,6,5, Ctype.disjunction, Ctype.disjunction,
            Ctype.disjunction, Ctype.disjunction, Ctype.disjunction,
            Ctype.disjunction, Ctype.disjunction);

        connectfourfull.runit(4,5,6, Ctype.disjunction, Ctype.disjunction,
            Ctype.disjunction, Ctype.disjunction, Ctype.disjunction,
            Ctype.disjunction, Ctype.disjunction);*/
    }
}

class connectfourfullor2
{
    // two large runs
    public static void main(String[] args)
    {
      int rows = 4;// Default number of rows
      int cols = 4;// Default number of cols
      boolean bFile = false;
      String fileName = "";
      boolean bPureValue = false;
      boolean bHeurisitic = false;
      boolean bPrint = false;
      if (args.length != 0) {
        for (int i=0; i<args.length; i++) {
          if (args[i].equals("-print")) bPrint = true;
          if (args[i].equals("-pv")) bPureValue = true;
          if (args[i].equals("-heur")) bHeurisitic = true;
          if (args[i].equals("-row")) {
            rows = Integer.parseInt(args[i+1]);
            i++;
          }
          if (args[i].equals("-col")) {
            cols = Integer.parseInt(args[i+1]);
            i++;
          }
          if (args[i].equals("-file")) {
            fileName = args[i+1];
            i++;
            bFile = true;
          }
        }
      }
      connectfourfull.runit(bFile,fileName,bPureValue,bHeurisitic,bPrint,4,cols,rows, Ctype.disjunction, Ctype.disjunction,
          Ctype.disjunction, Ctype.disjunction, Ctype.disjunction,
          Ctype.disjunction, Ctype.disjunction);

//      // warm up
//        connectfourfull.runit(4,4,4, Ctype.disjunction, Ctype.disjunction,
//            Ctype.disjunction, Ctype.disjunction, Ctype.disjunction,
//            Ctype.disjunction, Ctype.disjunction);
//
//        connectfourfull.runit(4, 4, 4, Ctype.sqgac);
//
//        // the real thing col=5 row=6
//        connectfourfull.runit(4,5,6, Ctype.disjunction);
//        connectfourfull.runit(4,5,6, Ctype.sqgac);
    }
}

class connectfourfull_bordeaux
{
    // compare against bordeaux decomposition.
    public static void main(String[] args)
    {
        // warm up
        connectfourfull.runit(4,4,4, Ctype.disjunction, Ctype.disjunction,
            Ctype.disjunction, Ctype.disjunction, Ctype.disjunction,
            Ctype.disjunction, Ctype.disjunction);

        // the real thing.
        connectfourfull.runit(4,4,4, Ctype.disjunction, Ctype.disjunction,
            Ctype.bordeaux, Ctype.disjunction, Ctype.disjunction,
            Ctype.disjunction, Ctype.disjunction);
    }
}

class connectfourfull_encode
{
    public static void main(String[] args)
    {
        connectfourfull.runit(4,4,4, Ctype.sqgac, Ctype.sqgac,
            Ctype.sqgac, Ctype.sqgac, Ctype.sqgac,
            Ctype.sqgac, Ctype.sqgac, true);
    }
}

public class connectfourfull
{
    public static void main(String[] args)
    {
        runit(4, 4, 4, Ctype.sqgac);
        runit(4, 4, 4, Ctype.sqgac);
        runit(4, 4, 4, Ctype.nightingale);
        runit(4, 4, 4, Ctype.nightingale);
        runit(4, 4, 4, Ctype.nightingaleOneList);
        runit(4, 4, 4, Ctype.nightingaleOneList);
        runit(4, 4, 4, Ctype.gspredicate);
        runit(4, 4, 4, Ctype.gspredicate);
        runit(4, 4, 4, Ctype.gspositive);
        runit(4, 4, 4, Ctype.gspositive);

        // assume everything warmed up from here.

        runit(4, 4, 5, Ctype.sqgac);
        runit(4, 4, 5, Ctype.gspredicate);
        runit(4, 4, 5, Ctype.gspositive);
        runit(4, 4, 5, Ctype.nightingale);
        runit(4, 4, 5, Ctype.nightingaleOneList);

        runit(4, 5, 4, Ctype.sqgac);
        runit(4, 5, 4, Ctype.gspredicate);
        runit(4, 5, 4, Ctype.gspositive);
        runit(4, 5, 4, Ctype.nightingale);
        runit(4, 5, 4, Ctype.nightingaleOneList);

        runit(4, 5, 5, Ctype.sqgac);
        runit(4, 5, 5, Ctype.nightingale);
        runit(4, 5, 5, Ctype.nightingaleOneList);
        runit(4, 5, 5, Ctype.gspositive);
        runit(4, 5, 5, Ctype.gspredicate);
    }

    public static void mkconstraint(qcsp prob, Ctype c, mid_domain [] gsvars, predicate_wrapper pred)
    {
        if(c==Ctype.sqgac)
        {
            sqgac cons= new sqgac(gsvars, prob, pred);
        }
        else if(c==Ctype.gspredicate)
        {
            gac_schema_predicate cons= new gac_schema_predicate(gsvars, prob, pred);
        }
        else if(c==Ctype.gspositive)
        {
            gac_schema_positive cons= new gac_schema_positive(gsvars, prob, pred);
        }
        else if(c==Ctype.nightingale)
        {
            nightingaletuples cons=new nightingaletuples(gsvars, prob, pred, true, true);
        }
        else if(c==Ctype.nightingaleOneList)
        {
            nightingaletuples cons=new nightingaletuples(gsvars, prob, pred, false, true);
        }
        else if(c==Ctype.tableconstraint)
        {
            table_constraint cons=new table_constraint(gsvars, prob, pred);
        }
        else
        {
            System.out.println("Wrong constraint type.");
            assert false;
        }
    }

    static void runit(int connect, int cols, int rows, Ctype constype)
    {runit(false, "", true, true, false, connect, cols, rows, constype, constype, constype, constype, constype, constype, constype, false); }

    static void runit(int connect, int cols, int rows,
        Ctype findlinecons, Ctype movecons, Ctype shadow, Ctype linkheights, Ctype propboard, Ctype gamestatecons, Ctype lineor)
    {runit(false, "", true, true, false, connect, cols, rows, findlinecons, movecons, shadow, linkheights, propboard, gamestatecons, lineor, false);}

    static void runit(boolean bFile, String fileName, boolean bPureValue, boolean bHeurisitic, boolean bPrint, int connect, int cols, int rows,
        Ctype findlinecons, Ctype movecons, Ctype shadow, Ctype linkheights,
        Ctype propboard, Ctype gamestatecons, Ctype lineor)
    {runit(bFile, fileName, bPureValue, bHeurisitic, bPrint, connect, cols, rows, findlinecons, movecons, shadow, linkheights, propboard, gamestatecons, lineor, false);}

    static void runit(int connect, int cols, int rows,
        Ctype findlinecons, Ctype movecons, Ctype shadow, Ctype linkheights,
        Ctype propboard, Ctype gamestatecons, Ctype lineor, boolean binaryencoding)
    {runit(false, "", true, true, false, connect, cols, rows, findlinecons, movecons, shadow, linkheights, propboard, gamestatecons, lineor, binaryencoding);}

    static void runit(boolean bFile, String fileName, boolean bPureValue, boolean bHeurisitic, boolean bPrint, int connect, int cols, int rows,
        Ctype findlinecons, Ctype movecons, Ctype shadow, Ctype linkheights,
        Ctype propboard, Ctype gamestatecons, Ctype lineor, boolean binaryencoding)
    {
        qcsp prob;
        if (bPrint)
          prob = new c4(bPrint, rows, cols);
        else
          prob = new c4(rows, cols);

        System.out.println("Called for connect:"+connect+" rows:"+rows+" cols:"+cols+" cons types:"+findlinecons+movecons+shadow+linkheights+propboard+gamestatecons+lineor);
        constraint big_constraint=null;
        mid_domain[] a_moves=new mid_domain[rows*cols]; // universal move variables, to be shadowed by the existential ones below.

        mid_domain[] moves=new mid_domain[rows*cols];

        mid_domain zero=new existential(0, 0, prob, "zero"); // useful constant.
        mid_domain zero2=new existential(0, 0, prob, "zero2"); // for encoding, can't have two same variables in one constraint.

        mid_domain one=new existential(1, 1, prob, "one"); // useful constant.
        mid_domain two=new existential(2, 2, prob, "two"); // useful constant.

        mid_domain[][][] board=new mid_domain[rows*cols][][];   // board[move][column][row] *after* move.

        mid_domain[][] height=new mid_domain[rows*cols][];      // height after the move.

        mid_domain[] gamestate=new mid_domain[rows*cols];  // 0 = first player has won, 1 = second player has won. 2 = neither.

        mid_domain[] line=new mid_domain[rows*cols];  // whether a line exists at move n.

        line[0]=zero;

        boolean newlookahead=false;
        boolean endlookahead=false;

        assert rows>=4 && cols>=4; // must be allowed vertical and horizontal lines or the logic goes wrong.

      // If a file is given we take the moves from the file
      Vector<Integer> recordedMoves = new Vector<Integer>();
      int nbRecordedMoves = 0;
      if (bFile) {
        try {
          Scanner scanner = new Scanner(new File(fileName));
          while(scanner.hasNextInt()){
             recordedMoves.add(scanner.nextInt());
          };
        } catch (FileNotFoundException e) {
          e.printStackTrace();
        };
        nbRecordedMoves = recordedMoves.size();
      }


        for(int move=0; move< rows*cols; move++)
        {
          if (nbRecordedMoves > move) {
            if((move%2)==1)  // odd numbers
            {
                a_moves[move]=new universal(recordedMoves.get(move), recordedMoves.get(move), prob, "a_move"+move);
            }
            moves[move]=new existential(recordedMoves.get(move), recordedMoves.get(move), prob, "move"+move);
          } else {
            if((move%2)==1)  // odd numbers
            {
                a_moves[move]=new universal(cols, prob, "a_move"+move);
            }
            moves[move]=new existential(cols, prob, "move"+move);
          }


            // board state mid_domains
            board[move]=new mid_domain[cols][];
            for(int column=0; column<cols; column++)
            {
                board[move][column]=new mid_domain[rows];
                for(int row=0; row<rows; row++)
                {
                    board[move][column][row]=new existential(3, prob, "board["+move+"]["+column+"]["+row+"]");
                }
            }

            // column heights
            height[move]=new mid_domain[cols];
            for(int column=0; column<cols; column++)
            {
                height[move][column]=new existential(rows+1, prob, "height["+move+"]["+column+"]");
            }

            // winstate per move *after* move.

            gamestate[move]=new existential(3, prob, "winstate"+move);

            //xline[move]=new existential(2, prob, "xline"+move);
            //oline[move]=new existential(2, prob, "oline"+move);

            // connect move variables to board variables.

            for(int col=0; col<cols; col++)
            {
                predicate_wrapper p1;
                if(move%2==0)
                    p1=new c4redmove_pred(col, rows);
                else
                    p1=new c4blackmove_pred(col, rows);
                mid_domain[] gsvars = new mid_domain[rows+3];
                if(move==0)
                {
                    gsvars[0]=zero;
                    gsvars[1]=zero2;
                }
                else
                {
                    gsvars[0]=line[move-1];
                    gsvars[1]=height[move-1][col];
                }
                gsvars[2]=moves[move];
                for(int i=0; i<rows; i++) gsvars[i+3]=board[move][col][i];

                if(movecons!=Ctype.disjunction)
                {
                    mkconstraint(prob, movecons, gsvars, p1);
                }
                else
                {assert movecons==Ctype.disjunction;
                    mid_domain mh=new existential(2, prob, "mh"+move+","+col);
                    //FIX mid_domain [] vars={move==0?zero:line[move-1], move==0?zero:height[move-1][col], moves[move], mh};
                    //FIX boolean [] neg={false, false, true, true};
                    //FIX int [] vals={1, rows, col, 1};
                    //FIX or_constraint_values cons1=new or_constraint_values(vars, vals, neg, 3, prob);
                    mid_domain [] vars={move==0?zero:height[move-1][col], moves[move], mh};
                    boolean [] neg={false, true, true};
                    int [] vals={rows, col, 1};
                    or_constraint_values cons1=new or_constraint_values(vars, vals, neg, 2, prob);

                    for(int row=0; row<rows; row++)
                    {
                        // height[move-1][col]==row => rh
                        {mid_domain rh=new existential(2, prob, "rh"+move+","+col+","+row);
                        mid_domain [] vars1={move==0?zero:height[move-1][col], rh, one};
                        boolean [] neg1={true, false, false};
                        int [] vals1={row, 1, 1};
                        or_constraint_values cons2=new or_constraint_values(vars1, vals1, neg1, 2, prob);

                        // rh <=> board

                        mid_domain [] vars2=new mid_domain[rows-row+1]; vars2[0]=rh;
                        for(int localrow=row; localrow<rows; localrow++) vars2[localrow-row+1]=board[move][col][localrow];
                        boolean [] neg2= new boolean[rows-row+1]; Arrays.fill(neg2, true); neg2[1]=false;
                        int [] vals2= new int[rows-row+1]; vals2[0]=1; vals2[1]=(move%2==0)?1:0;  // the other player's token
                        for(int i=2; i<vals2.length; i++) vals2[i]=2;
                        or_constraint_values cons5=new or_constraint_values(vars2, vals2, neg2, 0, prob);}

                        // mh & height[move-1][col]=row  => board=thisplayer

                        mid_domain [] vars1={mh, move==0?zero:height[move-1][col], board[move][col][row], one};
                        boolean [] neg1={true, true, false, false};
                        int [] vals1={1, row, (move%2==0)?0:1, 1};
                        or_constraint_values cons2=new or_constraint_values(vars1, vals1, neg1, 3, prob);

                        // !mh & height[move-1][col]=row => board=nil
                        mid_domain [] vars2={mh, move==0?zero:height[move-1][col], board[move][col][row], one};
                        boolean [] neg2={false, true, false, false};
                        int [] vals2={1, row, 2, 1};
                        or_constraint_values cons3=new or_constraint_values(vars2, vals2, neg2, 3, prob);


                        // The following four constraints are semantically correct but have
                        // poor propagation because they don't set anything in the column until
                        // mh is instantiated.
                        // These two constraints deal with the case where a move is made in this col.
                        /*{mid_domain mh1=new existential(2, prob, "mh"+move+","+col+","+row);
                        mid_domain [] vars1={mh, move==0?zero:height[move-1][col], mh1, one};
                        boolean [] neg1={true, true, false, false};
                        int [] vals1={1, row, 1, 1};
                        or_constraint_values cons2=new or_constraint_values(vars1, vals1, neg1, 3, prob);

                        mid_domain [] vars2=new mid_domain[rows-row+1]; vars2[0]=mh1;
                        for(int localrow=row; localrow<rows; localrow++) vars2[localrow-row+1]=board[move][col][localrow];
                        boolean [] neg2= new boolean[rows-row+1]; Arrays.fill(neg2, true);
                        int [] vals2= new int[rows-row+1]; vals2[0]=1;
                        vals2[1]=(move%2==0)?0:1;
                        for(int i=2; i<vals2.length; i++) vals2[i]=2;
                        or_constraint_values cons3=new or_constraint_values(vars2, vals2, neg2, 0, prob);}

                        // These two deal with no move being made here. Fill the remaining spaces in the col with 2's.

                        mid_domain fillcol=new existential(2, prob, "fillcol"+move+","+col+","+row);
                        mid_domain [] vars1={mh, move==0?zero:height[move-1][col], fillcol, one};
                        boolean [] neg1={false, true, false, false};
                        int [] vals1={1, row, 1, 1};
                        or_constraint_values cons2=new or_constraint_values(vars1, vals1, neg1, 3, prob);

                        mid_domain [] vars2=new mid_domain[rows-row+1]; vars2[0]=fillcol;
                        for(int localrow=row; localrow<rows; localrow++) vars2[localrow-row+1]=board[move][col][localrow];
                        boolean [] neg2= new boolean[rows-row+1]; Arrays.fill(neg2, true);
                        int [] vals2= new int[rows-row+1]; vals2[0]=1;
                        //vals2[1]=(move%2==0)?0:1;
                        for(int i=1; i<vals2.length; i++) vals2[i]=2;
                        or_constraint_values cons5=new or_constraint_values(vars2, vals2, neg2, 0, prob);*/
                    }
                }
            }

            if(move%2==1)
            {   // black move.
                //connect a_moves[move] to moves[move]

                for(int col=0; col<cols; col++)
                {
                    predicate_wrapper p1=new c4purecons_pred(col, rows);
                    mid_domain[] gsvars = new mid_domain[4];

                    gsvars[0]=gamestate[move-1];
                    gsvars[1]=height[move-1][col];
                    gsvars[2]=a_moves[move];
                    gsvars[3]=moves[move];
                    if(shadow!=Ctype.disjunction && shadow!=Ctype.bordeaux)
                    {
                        mkconstraint(prob, shadow, gsvars, p1);
                    }
                    else if(shadow==Ctype.disjunction)
                    {
                        mid_domain [] vars={one, gamestate[move-1], height[move-1][col], a_moves[move], moves[move]};
                        boolean [] neg={false, true, false, true, false};
                        int [] vals={1, 2, rows, col, col};
                        or_constraint_values cons1=new or_constraint_values(vars, vals, neg, 0, prob);
                    }
                    else
                    {   assert shadow==Ctype.bordeaux;
                        mid_domain [] t=new mid_domain[7];  // temp variables
                        for(int i=1; i<=6; i++)
                            t[i]=new existential(2, prob, "t"+i);

                        {mid_domain [] vars1={t[1], gamestate[move-1]};
                        int [] vals1={1, 2};
                        boolean [] neg1={false, true};
                        or_constraint_values cons1=new or_constraint_values(vars1, vals1, neg1, 0, prob);}

                        {mid_domain [] vars1={t[2], height[move-1][col]};
                        int [] vals1={1, rows};
                        boolean [] neg1={false, false};
                        or_constraint_values cons1=new or_constraint_values(vars1, vals1, neg1, 0, prob);}

                        {mid_domain [] vars1={a_moves[move], t[3]};
                        int [] vals1={cols, 1};
                        boolean [] neg1={true, false};
                        or_constraint_values cons1=new or_constraint_values(vars1, vals1, neg1, 1, prob);}

                        {mid_domain [] vars1={t[4], moves[move]};
                        int [] vals1={1, cols};
                        boolean [] neg1={false, false};
                        or_constraint_values cons1=new or_constraint_values(vars1, vals1, neg1, 0, prob);}

                        {mid_domain [] vars1={t[1], t[2], t[5]};
                        boolean [] neg1={false, false, false};
                        or_constraint cons1=new or_constraint(vars1, neg1, 2, prob);}

                        {mid_domain [] vars1={t[3], t[5], t[6]};
                        boolean [] neg1={false, false, false};
                        or_constraint cons1=new or_constraint(vars1, neg1, 2, prob);}

                        {mid_domain [] vars1={t[4], t[6], one};
                        boolean [] neg1={false, false, false};
                        or_constraint cons1=new or_constraint(vars1, neg1, 2, prob);}
                    }
                }
            }

            // board state -- map existing pieces forward.

            if(move>0)
            {
            for(int col=0; col<cols; col++)
            {
                for(int row=0; row<rows; row++)
                {
                    predicate_wrapper p1=new c4boardstate_pred();
                    mid_domain[] gsvars = {board[move-1][col][row], board[move][col][row]};

                    if(propboard!=Ctype.disjunction)
                    {
                        mkconstraint(prob, propboard, gsvars, p1);
                    }
                    else
                    {
                        mid_domain[] vars={board[move-1][col][row], board[move][col][row], one};
                        int [] vals1={0, 0, 1};
                        int [] vals2={1, 1, 1};
                        boolean [] neg={true, false, false};
                        or_constraint_values cons1=new or_constraint_values(vars, vals1, neg, 2, prob);
                        or_constraint_values cons2=new or_constraint_values(vars, vals2, neg, 2, prob);
                    }
                }
            }}

            // Map forward the height variables.

            /*if(move>0)
            {
            for(int col=0; col<cols; col++)
            {
                {predicate_wrapper p1=new c4reviseheight_pred(col, rows);
                mid_domain[] gsvars = new mid_domain[4];
                gsvars[0]=line[move-1];
                gsvars[1]=height[move-1][col];
                gsvars[2]=moves[move];
                gsvars[3]=height[move][col];

                if(sqgac)
                {
                    sqgac cons= new sqgac(gsvars, prob, p1);
                }
                else
                {
                    gac_schema_predicate cons= new gac_schema_predicate(gsvars, prob, p1);
                }
                }
            }}
            else
            {
            for(int col=0; col<cols; col++)
            {
                // first move--use zero for the previous height
                {predicate_wrapper p1=new c4reviseheight_pred(col, rows);
                mid_domain[] gsvars = new mid_domain[4];
                gsvars[0]=zero;
                gsvars[1]=zero;
                gsvars[2]=moves[move];
                gsvars[3]=height[move][col];

                if(sqgac)
                {
                    sqgac cons= new sqgac(gsvars, prob, p1);
                }
                else
                {
                    gac_schema_predicate cons= new gac_schema_predicate(gsvars, prob, p1);
                }
                }
            }}*/

            // link heights to the board.
            for(int col=0; col<cols; col++)
            {
                predicate_wrapper p1=new c4linkheightboard_pred(rows);
                mid_domain[] gsvars = new mid_domain[rows+1];
                for(int row=0; row<rows; row++) gsvars[row]=board[move][col][row];

                gsvars[rows]=height[move][col];
                if(linkheights!=Ctype.disjunction)
                {
                    mkconstraint(prob, linkheights, gsvars, p1);
                }
                else
                {
                    for(int row=0; row<=rows; row++)
                    {
                        mid_domain [] vars={ (row==0)?zero:board[move][col][row-1], (row==rows)?two:board[move][col][row], height[move][col] };
                        int [] values={2, 2, row};
                        boolean [] neg={false, true, true};
                        or_constraint_values c1=new or_constraint_values(vars, values, neg, 2, prob);
                    }
                }
            }

            if(move>0)
            {
            // detect lines and map onto gamestate var.
            int diags=(rows-3+cols-3-1)*2;  // each dimension, minus 3, then minus the one shared one. In both directions.
            diags=(diags<0)?0:diags;
            assert diags>0;

            int numcons=0;  // counter
            ArrayList<mid_domain> temps= new ArrayList<mid_domain>(); // boolean vars which indicate a line
            int countercol = move%2;  // the colour of counter for this move.
            // horizontal lines
            for(int row=0; row<rows; row++)
            {
                if(findlinecons!=Ctype.disjunction)
                {predicate_wrapper p1=new c4findline_pred(connect);
                mid_domain[] gsvars = new mid_domain[cols+2];
                temps.add(new existential(2, prob, "linei"+move+","+numcons));
                gsvars[0]=line[move-1];
                for(int i=0; i<cols; i++) gsvars[i+1]=board[move][i][row];
                gsvars[cols+1]=temps.get(numcons);

                mkconstraint(prob, findlinecons, gsvars, p1);

                numcons++;
                }
                else
                {// use disjunction constraints.
                assert connect==4;
                for(int col=0; col<cols-3; col++)
                {   temps.add(new existential(2, prob, "linei"+move+","+numcons));
                    int [] values={1, countercol, countercol, countercol, countercol, 1};
                    mid_domain[] gsvars = {line[move-1], board[move][col][row], board[move][col+1][row],
                        board[move][col+2][row], board[move][col+3][row], temps.get(numcons)};
                    boolean [] neg={false, true, true, true, true, true};
                    or_constraint_values cons= new or_constraint_values(gsvars, values, neg, 5, prob);
                    numcons++;
                }
                }
            }

            // vertical lines
            for(int col=0; col<cols; col++)
            {
                if(findlinecons!=Ctype.disjunction)
                {predicate_wrapper p1=new c4findline_pred(connect);
                mid_domain[] gsvars = new mid_domain[rows+2];
                gsvars[0]=line[move-1];
                for(int i=0; i<rows; i++) gsvars[i+1]=board[move][col][i];
                temps.add(new existential(2, prob, "linei"+move+","+numcons));
                gsvars[rows+1]=temps.get(numcons);

                mkconstraint(prob, findlinecons, gsvars, p1);

                numcons++;
                }
                else
                {
                for(int row=0; row<rows-3; row++)
                {   temps.add(new existential(2, prob, "linei"+move+","+numcons));
                    int [] values={1, countercol, countercol, countercol, countercol, 1};
                    mid_domain[] gsvars = {line[move-1], board[move][col][row], board[move][col][row+1],
                        board[move][col][row+2], board[move][col][row+3], temps.get(numcons)};
                    boolean [] neg={false, true, true, true, true, true};
                    or_constraint_values cons= new or_constraint_values(gsvars, values, neg, 5, prob);
                    numcons++;
                }
                }
            }

            // diagonal lines --downward from left diagonal.\\\\
            for(int col=cols-4; col>0; col--)  // do not include 0.
            {
                if(findlinecons!=Ctype.disjunction)
                {predicate_wrapper p1=new c4findline_pred(connect);
                int length=0;
                for(int i=0; (i<rows && col+i<cols); i++) length++;
                mid_domain[] gsvars = new mid_domain[length+2];
                gsvars[0]=line[move-1];
                for(int i=0; (i<rows && col+i<cols); i++) gsvars[i+1]=board[move][col+i][rows-i-1];
                temps.add(new existential(2, prob, "linei"+move+","+numcons));
                gsvars[length+1]=temps.get(numcons);

                mkconstraint(prob, findlinecons, gsvars, p1);

                numcons++;
                }
                else
                {
                for(int numline=0; (numline<cols-col-3 && rows-1-3-numline>=0); numline++)
                {   temps.add(new existential(2, prob, "linei"+move+","+numcons));
                    int row=rows-1;   // which row to start on
                    int [] values={1, countercol, countercol, countercol, countercol, 1};
                    mid_domain[] gsvars = {line[move-1],
                        board[move][col+numline][row-numline],
                        board[move][col+1+numline][row-1-numline],
                        board[move][col+2+numline][row-2-numline],
                        board[move][col+3+numline][row-3-numline], temps.get(numcons)};
                    boolean [] neg={false, true, true, true, true, true};
                    or_constraint_values cons= new or_constraint_values(gsvars, values, neg, 5, prob);
                    numcons++;
                }
                }
            }
            for(int row=rows-1; row>2; row--)
            {
                if(findlinecons!=Ctype.disjunction)
                {predicate_wrapper p1=new c4findline_pred(connect);
                int length=0;
                for(int i=0; (i<cols && row-i>=0); i++) length++;
                mid_domain[] gsvars = new mid_domain[length+2];
                temps.add(new existential(2, prob, "linei"+move+","+numcons));
                gsvars[0]=line[move-1];
                for(int i=0; (i<cols && row-i>=0); i++) gsvars[i+1]=board[move][i][row-i];
                gsvars[length+1]=temps.get(numcons);

                mkconstraint(prob, findlinecons, gsvars, p1);

                numcons++;
                }
                else
                {
                for(int numline=0; (numline<cols-0-3 && row-3-numline>=0); numline++)
                {   temps.add(new existential(2, prob, "linei"+move+","+numcons));
                    int col=0;   // which col to start on
                    int [] values={1, countercol, countercol, countercol, countercol, 1};
                    mid_domain[] gsvars = {line[move-1],
                        board[move][col+numline][row-numline],
                        board[move][col+1+numline][row-1-numline],
                        board[move][col+2+numline][row-2-numline],
                        board[move][col+3+numline][row-3-numline], temps.get(numcons)};
                    boolean [] neg={false, true, true, true, true, true};
                    or_constraint_values cons= new or_constraint_values(gsvars, values, neg, 5, prob);
                    numcons++;
                }
                }
            }

            // other diagonal, i.e. //
            for(int row=3; row<rows; row++)  // includes the shared one.
            {
                if(findlinecons!=Ctype.disjunction)
                {predicate_wrapper p1=new c4findline_pred(connect);
                int length=0;
                for(int i=0; (row-i>=0 && cols-i-1>=0); i++) length++;
                mid_domain[] gsvars = new mid_domain[length+2];
                gsvars[0]=line[move-1];
                for(int i=0; (row-i>=0 && cols-i-1>=0); i++) gsvars[i+1]=board[move][cols-i-1][row-i];
                temps.add(new existential(2, prob, "linei"+move+","+numcons));
                gsvars[length+1]=temps.get(numcons);

                mkconstraint(prob, findlinecons, gsvars, p1);

                numcons++;
                }
                else
                {
                for(int numline=0; (cols-1-3-numline>=0 && row-3-numline>=0); numline++)
                {   temps.add(new existential(2, prob, "linei"+move+","+numcons));
                    int col=cols-1;   // which col to start on
                    int [] values={1, countercol, countercol, countercol, countercol, 1};
                    mid_domain[] gsvars = {line[move-1],
                        board[move][col-numline][row-numline],
                        board[move][col-1-numline][row-1-numline],
                        board[move][col-2-numline][row-2-numline],
                        board[move][col-3-numline][row-3-numline], temps.get(numcons)};
                    boolean [] neg={false, true, true, true, true, true};
                    or_constraint_values cons= new or_constraint_values(gsvars, values, neg, 5, prob);
                    numcons++;
                }
                }
            }
            for(int col=cols-2; col>=3; col--)
            {
                if(findlinecons!=Ctype.disjunction)
                {predicate_wrapper p1=new c4findline_pred(connect);
                int length=0;
                for(int i=0; (rows-i-1>=0 && col-i>=0); i++) length++;
                mid_domain[] gsvars = new mid_domain[length+2];
                gsvars[0]=line[move-1];
                for(int i=0; (rows-i-1>=0 && col-i>=0); i++) gsvars[i+1]=board[move][col-i][rows-i-1];
                temps.add(new existential(2, prob, "linei"+move+","+numcons));
                gsvars[length+1]=temps.get(numcons);

                mkconstraint(prob, findlinecons, gsvars, p1);

                numcons++;
                }
                else
                {
                for(int numline=0; (col-3-numline>=0 && rows-1-3-numline>=0); numline++)
                {   temps.add(new existential(2, prob, "linei"+move+","+numcons));
                    int row=rows-1;   // which col to start on
                    int [] values={1, countercol, countercol, countercol, countercol, 1};
                    mid_domain[] gsvars = {line[move-1],
                        board[move][col-numline][row-numline],
                        board[move][col-1-numline][row-1-numline],
                        board[move][col-2-numline][row-2-numline],
                        board[move][col-3-numline][row-3-numline], temps.get(numcons)};
                    boolean [] neg={false, true, true, true, true, true};
                    or_constraint_values cons= new or_constraint_values(gsvars, values, neg, 5, prob);
                    numcons++;
                }
                }
            }
            assert findlinecons==Ctype.disjunction || (numcons==rows+cols+diags && temps.size()==rows+cols+diags);
            assert numcons==temps.size();
            // now conjunct the temps
            // set gamestate to x if it is not already set and there is a line.
            // split the long disjunction into binary chunks.
            if(lineor!=Ctype.disjunction)
            {
                mid_domain carry=null;
                for(int chunk=0; chunk<temps.size(); chunk++)
                {
                    mid_domain [] disjunction=new mid_domain[3];
                    if(chunk==0) disjunction[0]=line[move-1];
                    else disjunction[0]=carry;
                    disjunction[1]=temps.get(chunk);
                    carry=new existential(2, prob, "line"+move);
                    disjunction[2]=carry;

                    boolean[] neg=new boolean[3];
                    Arrays.fill(neg, false);
                    or_predicate p1=new or_predicate(neg, 2);

                    mkconstraint(prob, lineor, disjunction, p1);
                }
                line[move]=carry;
            }
            else
            {
                mid_domain [] disjunction=new mid_domain[temps.size()+2];
                disjunction[0]=line[move-1];
                for(int i=0; i<temps.size(); i++) disjunction[i+1]=temps.get(i);
                line[move]=new existential(2, prob, "line"+move);

                disjunction[temps.size()+1]=line[move];
                boolean[] neg=new boolean[temps.size()+2]; Arrays.fill(neg, false);

                constraint big_or=new or_constraint(disjunction, neg, temps.size()+1, prob);
            }

            if(move%2==0) // red move. Connect t1 to gamestate[move-1] and gamestate[move]
            {
                mid_domain[] vars={gamestate[move-1], line[move], gamestate[move]};
                predicate_wrapper p1=new c4gamestate_pred(0);

                if(gamestatecons!=Ctype.disjunction)
                {
                    mkconstraint(prob, gamestatecons, vars, p1);
                }
                else
                {   assert gamestatecons==Ctype.disjunction;
                    System.out.println(move-1);
                    mid_domain [] vars1={gamestate[move-1], line[move], gamestate[move], one};
                    int [] vals1={2, 1, 0, 1};
                    boolean []neg1={true, true, false, false};
                    or_constraint_values c1=new or_constraint_values(vars1, vals1, neg1, 3, prob);  // gs1=2 && line => gs2=0

                    int [] vals2={2, 1, 2, 1};
                    boolean []neg2={true, false, false, false};
                    or_constraint_values c2=new or_constraint_values(vars1, vals2, neg2, 3, prob);  // gs1=2 && neg:line => gs2=2

                    mid_domain [] vars3={gamestate[move-1], gamestate[move], one};
                    int [] vals3={1, 1, 1};
                    boolean []neg3={true, false, false};
                    or_constraint_values c3=new or_constraint_values(vars3, vals3, neg3, 2, prob);  // gs1=1 => gs2=1

                    int [] vals4={0, 0, 1};
                    or_constraint_values c4=new or_constraint_values(vars3, vals4, neg3, 2, prob);  // gs1=0 => gs2=0
                }
            }
            else
            {
                // set gamestate to 1 if it is not already set and there is a new line.
                mid_domain[] vars={gamestate[move-1], line[move], gamestate[move]};
                predicate_wrapper p1=new c4gamestate_pred(1);
                if(gamestatecons!=Ctype.disjunction)
                {
                    mkconstraint(prob, gamestatecons, vars, p1);
                }
                else
                {   assert gamestatecons==Ctype.disjunction;
                    mid_domain [] vars1={gamestate[move-1], line[move], gamestate[move], one};
                    int [] vals1={2, 1, 1, 1};
                    boolean []neg1={true, true, false, false};
                    or_constraint_values c1=new or_constraint_values(vars1, vals1, neg1, 3, prob);  // gs1=2 && line => gs2=1

                    int [] vals2={2, 1, 2, 1};
                    boolean []neg2={true, false, false, false};
                    or_constraint_values c2=new or_constraint_values(vars1, vals2, neg2, 3, prob);  // gs1=2 && neg:line => gs2=2

                    mid_domain [] vars3={gamestate[move-1], gamestate[move], one};
                    int [] vals3={1, 1, 1};
                    boolean []neg3={true, false, false};
                    or_constraint_values c3=new or_constraint_values(vars3, vals3, neg3, 2, prob);  // gs1=1 => gs2=1

                    int [] vals4={0, 0, 1};
                    or_constraint_values c4=new or_constraint_values(vars3, vals4, neg3, 2, prob);  // gs1=0 => gs2=0
                }
            }

            }// end move>0

            /////////////////////////////////////////////////////////////////////////////////
            // Intelligent play constraints

            // choose a move at random to begin with.
            /*if(move==11)
            {
                // four-move lookahead, looks at 4 move variables.
                assert (move-3)%2==0;  // first var is existential.
                c4lookahead_pred p1=new c4lookahead_pred(rows, cols);
                mid_domain[] gsvars = new mid_domain[rows*cols+3];
                p1.prob=prob;

                for(int col=0; col<cols; col++)
                {
                    for(int row=0; row<rows; row++)
                    {
                        gsvars[row+(col*rows)]=board[move-4][col][row];
                    }
                }
                gsvars[rows*cols]=moves[move-3];
                gsvars[rows*cols+1]=a_moves[move-2];
                gsvars[rows*cols+2]=moves[move-1];
                //gsvars[rows*cols+3]=a_moves[move];

                //if(false)
                //{
                //    sqgac cons= new sqgac(gsvars, prob, p1);
                //}
                //else
                //{
                    //gac_schema_predicate cons= new gac_schema_predicate(gsvars, prob, p1);
                    big_constraint= new gac_schema_predicate(gsvars, prob, p1);
                //}
            }*/

            // alternative lookahead.
            if(newlookahead && (move==12))// || move==14))
            {
                // three-move lookahead, looks at 4 move variables.
                // looks ahead from move 8 to move 11. (counting from 0).
                // and from move 12 to move 15. Last variable is collapsed away.
                assert (move-3)%2==1;  // first var is universal
                c4lookahead5_pred p1 = new c4lookahead5_pred(rows, cols);
                mid_domain[] gsvars = new mid_domain[rows*cols+2];

                for(int col=0; col<cols; col++)
                {
                    for(int row=0; row<rows; row++)
                    {
                        gsvars[row+(col*rows)]=board[move-2][col][row];
                    }
                }
                gsvars[rows*cols]  =  a_moves[move-1];
                gsvars[rows*cols+1] = moves[move];

                big_constraint= new gac_schema_predicate(gsvars, prob, p1);
            }

            if(endlookahead && move==rows*cols-2)
            {
                // three-move lookahead, looks at 4 move variables.
                // looks ahead from move 16 to 19 (the last move.
                assert (move-3)%2==1;  // first var is universal
                c4lookahead5_pred p1 = new c4lookahead5_pred(rows, cols, false);
                mid_domain[] gsvars = new mid_domain[rows*cols+2];

                for(int col=0; col<cols; col++)
                {
                    for(int row=0; row<rows; row++)
                    {
                        gsvars[row+(col*rows)]=board[move-2][col][row];
                    }
                }
                gsvars[rows*cols]  =  a_moves[move-1];
                gsvars[rows*cols+1] = moves[move];

                big_constraint= new gac_schema_predicate(gsvars, prob, p1);
            }
        }

        if (bPureValue) prob.pure_setup_universals();  // enables the pure value rule, could take some time to execute

        if (bHeurisitic) {
          heuristic h1=new c4heuristic(rows, cols, board);
          prob.setHeuristic(h1);
        }

        //System.out.println(prob.constraints.toString());

        // root node processing here.
        //System.out.println("Establish GAC list:");
        //System.out.println(prob.queue.toestablish);

        stopwatch sw= new stopwatch("GMT");

        sw.start();
        boolean flag=prob.establish();
        //prob.printdomains();
        assert flag;

        // set some variables

        if(gamestate[0].is_present(0)) gamestate[0].exclude(0, null);
        if(gamestate[0].is_present(1)) gamestate[0].exclude(1, null);

        gamestate[rows*cols-1].exclude(1, null); // make sure red wins.
        gamestate[rows*cols-1].exclude(2, null);

        // symmetry breaking
        if (!bFile) { // break symmetry only for initial empty board
          if(cols%2==0)
          {   // even width
            // first move on left hand side of board.
            for(int i=(cols/2); i<cols; i++)
              if(moves[0].is_present(i)) moves[0].exclude(i, null);
          }
          else
          {
            // the first player who does not play in the centre, plays on the left. Is what it should be.

            System.out.println("WARNING: this does not entirely break the symmetry for problems where the number of cols is odd.");
            for(int i=(cols/2)+1; i<cols; i++)
              if(moves[0].is_present(i)) moves[0].exclude(i, null);
          }
        }

        {mid_domain [][] lastboard=board[rows*cols-1];
        // set the last board full.
        for(int col=0; col<cols; col++)
        {
            for(int row=0; row<rows; row++)
            {
                flag=lastboard[col][row].exclude(2, null);  // no empty places.
                assert flag;
            }

            /*for(int h=0; h<rows; h++)
            {
                boolean flag=height[rows*cols-1][col].exclude(h, null);
                assert flag;
            }*/
        }
        }

        prob.add_backtrack_level();
//        prob.printdomains();
        flag=prob.propagate();
//        prob.printdomains();
        assert flag;

        sw.end();

        System.out.println("Setup time: "+sw.elapsedMillis());

        if(binaryencoding) encoding.qcspencodeproblem(prob);
        else
        {
        sw= new stopwatch("GMT");

        sw.start();
        System.out.println(prob.search());

        //moves[0].instantiate(0);

        //prob.propagate();

        //prob.printdomains();

        sw.end();
        System.out.println("Search time: "+sw.elapsedMillis()+" , Nodes:"+prob.numnodes);
        System.out.println("Propagations: "+prob.propagations);

        prob.savetree("connectfourfull.dot");

        }
        //System.out.println(cons);
    }
}

/*class c4firstredmove_pred implements predicate_wrapper
{
    c4firstredmove_pred(int col)
    {
        this.col=col;
    }
    final int col;

    public boolean predicate(tuple tau)
    {
        // first var is the move.
        if(tau.vals[0]==col)
        {
            // do the move.
            return tau.vals[1]==0;  // first player is 0.
        }

        return true;
    }
}*/

class c4redmove_pred implements predicate_wrapper
{
    c4redmove_pred(int col, int rows)
    {
        this.col=col;
        this.rows=rows;
    }

    final int col; // the column of this move.
    final int rows;

    public boolean predicate(tuple tau)
    {
        // tau[0] is the previous line variable
        // tau[1] is the previous column height
        // tau[2] is the move variable
        // tau[3...] are the new vars in the column, from the base.
        if(tau.vals[0]==1)  // there is already a line
        {
            // fill in the column with 2's
            for(int i=tau.vals[1]+3; i<tau.vals.length; i++)
            {
                if(!(tau.vals[i]==2))
                    return false;
            }
            return true;
        }
        // first var is the height of the existing stack.
        if(tau.vals[1]==rows)
        {
            return tau.vals[2]!=col;
        }

        if(tau.vals[2]==col)
        {
            // do the move. Then fill above with 2's.
            if(!(tau.vals[tau.vals[1]+3]==0))
                return false;

            for(int i=tau.vals[1]+4; i<tau.vals.length; i++)
            {
                if(!(tau.vals[i]==2))
                    return false;
            }
            return true;
        }
        else
        {
            // fill in the column with 2's
            for(int i=tau.vals[1]+3; i<tau.vals.length; i++)
            {
                if(!(tau.vals[i]==2))
                    return false;
            }
            return true;
        }
    }
}

class c4blackmove_pred implements predicate_wrapper
{
    c4blackmove_pred(int col, int rows)
    {
        this.col=col;
        this.rows=rows;
    }

    final int col; // the column of this move.
    final int rows;

    // only diff from above is that, when making a move, the board place is set to 1.

    public boolean predicate(tuple tau)
    {
        // tau[0] is the previous line variable
        // tau[1] is the previous column height
        // tau[2] is the move variable
        // tau[3...] are the new vars in the column, from the base.
        if(tau.vals[0]==1)  // there is already a line
        {
            // fill in the column with 2's
            for(int i=tau.vals[1]+3; i<tau.vals.length; i++)
            {
                if(!(tau.vals[i]==2))
                    return false;
            }
            return true;
        }
        // first var is the height of the existing stack.
        if(tau.vals[1]==rows)
        {
            return tau.vals[2]!=col;
        }

        if(tau.vals[2]==col)
        {
            // do the move. Then fill above with 2's.
            if(!(tau.vals[tau.vals[1]+3]==1))
                return false;

            for(int i=tau.vals[1]+4; i<tau.vals.length; i++)
            {
                if(!(tau.vals[i]==2))
                    return false;
            }
            return true;
        }
        else
        {
            // fill in the column with 2's
            for(int i=tau.vals[1]+3; i<tau.vals.length; i++)
            {
                if(!(tau.vals[i]==2))
                    return false;
            }
            return true;
        }
    }
}

class c4boardstate_pred implements predicate_wrapper
{
    // just copies the board state from one move to the next.
    // If there's a line,
    public boolean predicate(tuple tau)
    {
        if(tau.vals[0]!=2)
        {
            return tau.vals[0]==tau.vals[1];
        }
        return true;
    }
}

class c4reviseheight_pred implements predicate_wrapper
{
    c4reviseheight_pred(int col, int rows)
    {
        this.col=col;
        this.rows=rows;
    }
    final int col;
    final int rows;

    public boolean predicate(tuple tau)
    {
        if(tau.vals[0]==1) // if there is already a line
            return tau.vals[1]==tau.vals[3];

        if(tau.vals[1]<rows)
        {
            if(tau.vals[2]==col)
            {
                return tau.vals[1]+1==tau.vals[3];
            }
            else
            {   // do not add to the height.
                return tau.vals[1]==tau.vals[3];
            }
        }
        else
        {
            return tau.vals[3]==rows;  // no conflicts for the move variable
        }
    }
}

class c4linkheightboard_pred implements predicate_wrapper
{
    c4linkheightboard_pred(int rows)
    {
        this.rows=rows;
    }
    final int rows;

    public boolean predicate(tuple tau)
    {
        int height=rows;
        for(int row=0; row<rows; row++)
        {
            if(tau.vals[row]==2)
            {
                height=row;
                break;
            }
        }
        return tau.vals[rows]==height;
    }
}

class c4findline_pred implements predicate_wrapper
{
    c4findline_pred(int ll)
    {
        linelength=ll;
    }
    final int linelength;

    public boolean predicate(tuple tau)
    {
        if(tau.vals[0]==1) // if there is already a line in the last move,
            return tau.vals[tau.vals.length-1]==1;

        int len=tau.vals.length;
        int lastval=tau.vals[1];
        int count=1;
        int flag=0;
        for(int i=2; i<(len-1); i++)
        {
            int thisval=tau.vals[i];
            if(lastval==2)
            {
                if(thisval==0 || thisval==1)
                {
                    lastval=thisval;
                    count=1;
                }
            }
            else
            {
                if(lastval==thisval)
                {
                    count++;
                    if(count==linelength)
                    {
                        flag=1;
                        break;
                    }
                }
                else
                {
                    lastval=thisval;
                    count=1;
                }
            }
        }

        return tau.vals[len-1]==flag;
    }
}

class c4gamestate_pred implements predicate_wrapper
{
    c4gamestate_pred(int tosetval)
    {
        this.tosetval=tosetval;
    }
    final int tosetval;

    public boolean predicate(tuple tau)
    {
        assert tau.vals.length==3;
        if(tau.vals[0]==2)
        {
            if(tau.vals[1]==1)
            {
                return tau.vals[2]==tosetval;
            }
            else
            {
                return tau.vals[2]==2;
            }
        }
        else
        {
            return tau.vals[0]==tau.vals[2];
        }
    }
}

class c4purecons_pred implements predicate_wrapper
{
    c4purecons_pred(int value, int rows)
    {
        this.value=value;
        this.rows=rows;
    }
    final int value;
    final int rows;

    public boolean predicate(tuple tau)
    {
        // tau[0] is the previous gamestate
        // tau[1] is the previous height.
        // tau[2] is the universal move variable
        // tau[3] is the shadow move variable.
        if(tau.vals[0]==2 && tau.vals[1]!=rows) // game not won at last move, and this column not full.
        {
            if(tau.vals[2]==value)
                return tau.vals[3]==value;
            else
                return true;
        }
        else
        {
            // move is not available
            return true;
        }
    }
}

// intelligence constraints
// all other constraints up to here just set up a dumb solver playing within the rules.

// red does not pass up a chance to win. three boardstate vars, p1 p2 p3
// if p1=0 and p2=0 and p3=0 implies the following:
// (heightvar==(height) implies p4=0)
// i.e. red blocks the end of the line.

class c4trivialwin_pred implements predicate_wrapper
{
    c4trivialwin_pred(int height)
    {
        this.height=height;
    }
    final int height;

    public boolean predicate(tuple tau)
    {
        if(tau.vals[0]==0 && tau.vals[1]==height && tau.vals[2]==1 && tau.vals[3]==1 && tau.vals[4]==1) // not implied, appropriate height,
        {
            // and all 3 board state variables set appropriately,
            // then set the final piece to red i.e. block the line.
            return tau.vals[4]==0;
        }
        else
        {
            return true;
        }
    }
}

// The huge constraint. Must use WQGAC for this I think, so that it is loose to begin with
// and doesn't completely cripple the solver.
class c4lookahead_pred implements predicate_wrapper
{
    c4lookahead_pred(int rows, int cols)
    {
        this.rows=rows;
        this.cols=cols;

        // allocated here so it is done once only.
        board=new int[rows][];
        for(int row=0; row<rows; row++)
        {
            board[row]=new int[cols];
        }
    }

    final int rows;
    final int cols;
    final int [][] board;

    final boolean record=false;
    int numinvocations=0;
    qcsp prob;
    // diagram 1.
    // Two ways to win.

    // board state, 0 equals o and 1=x. 2=unoccupied.
    int counter=0;
    // board[row][column] = state

    public boolean predicate(tuple tau)
    {
        // First copy the first cols*rows vars into the board.
        // board[move][col][row] outside
        //System.out.println("Entered predicate");
        // this board is [row][col]
        //assert tau.vals.length==rows*cols+3;

        for(int col=0; col<cols; col++)
        {
            for(int row=0; row<rows; row++)
            {
                board[row][col]=tau.vals[row+(col*rows)];
            }
        }

        int move1=tau.vals[rows*cols];
        int move2=tau.vals[rows*cols+1];
        int move3=tau.vals[rows*cols+2];
        //int move4=tau.vals[rows*cols+3];
        if(record){ numinvocations++;System.out.println("invocations:"+numinvocations);}

        if(record)
        {
            //prob.printdomains();
            printboard();
            System.out.println("move1:"+move1+", move2:"+move2+", move3:"+move3);
        }

        if(checklines(board)) // this constraint gets out of the way if there is already a line.
        {
            return true;
        }

        // first move, o
        boolean flag=false;
        for(int i=0; i<rows; i++)
        {
            if(board[i][move1]==2)
            {
                board[i][move1]=0;
                flag=true;
                break;
            }
        }
        if(!flag) return false;

        //System.out.println("After first move:");
        //printboard();

        if(checklines(board))
        {
            return true;
        }

        if(record)
        {
            System.out.println("After move 1");
            printboard();
        }

        // second move
        flag=false;
        for(int i=0; i<rows; i++)
        {
            if(board[i][move2]==2)
            {
                board[i][move2]=1;
                flag=true;
                break;
            }
        }
        if(!flag) return true;

        if(checklines(board))
        {
            return false;
        }

        if(record)
        {
            System.out.println("After move 2");
            printboard();
        }

        // third move
        flag=false;
        for(int i=0; i<rows; i++)
        {
            if(board[i][move3]==2)
            {
                board[i][move3]=0;
                flag=true;
                break;
            }
        }
        if(!flag) return false;

        if(checklines(board))
        {
            return true;
        }

        if(record)
        {
            System.out.println("After move 3");
            printboard();
            //System.exit(0);
        }

        // expanded out fourth move.
        for(int move4=0; move4<cols; move4++)
        {
            if(board[rows-1][move4]==2)  // if this is an allowed move
            {
                if(record) {System.out.println("first board, move4="+move4); printboard();}
                flag=false;
                for(int i=0; i<rows; i++)
                {
                    if(board[i][move4]==2)
                    {
                        board[i][move4]=1;
                        flag=true;
                        break;
                    }
                }
                assert flag;
                if(record) {System.out.println("After move"); printboard();}
                if(checklines(board))
                {
                    return false;
                }

                // undo the move
                for(int i=0; i<=rows; i++)
                {
                    if(i==rows || board[i][move4]==2)
                    {
                        board[i-1][move4]=2;
                        break;
                    }
                }
                if(record) {System.out.println("After undo");printboard();}
            }
        }

        // fourth move-- not expanded
        /*flag=false;
        for(int i=0; i<rows; i++)
        {
            if(board[i][move4]==2)
            {
                board[i][move4]=1;
                flag=true;
                break;
            }
        }
        if(!flag) return true;

        if(checklines(board))
        {
            return false;
        }*/

        // if undecided after 4 moves,
        // then return true unless there is another winning move to make.
        // Just check the first move, if there is a line to complete.
        // The first move here is not a winning move.

        /*for(int col=0; col<cols; col++)
        {
            for(int row=0; row<rows; row++)
            {
                board[row][col]=tau.vals[row+(col*rows)];
            }
        }

        for(int col=0; col<cols; col++)
        {
            if(board[rows-1][col]==2 && col!=move1)
            {
                if(record) {System.out.println("first board, col="+col); printboard();}
                flag=false;
                for(int i=0; i<rows; i++)
                {
                    if(board[i][col]==2)
                    {
                        board[i][col]=1;
                        flag=true;
                        break;
                    }
                }
                assert flag;
                if(record) {System.out.println("After move"); printboard();}
                if(checklines(board)) // If the other move makes a line, then this move1 is not valid.
                {
                    return false;
                }

                // undo the move
                for(int i=0; i<=rows; i++)
                {
                    if(i==rows || board[i][col]==2)
                    {
                        board[i-1][col]=2;
                        break;
                    }
                }
                if(record) {System.out.println("After undo");printboard();}
            }
        }*/

        // make the first and second move again.
        // first move, o
        /*for(int i=0; i<rows; i++)
        {
            if(board[i][move1]==2)
            {
                board[i][move1]=0;
                break;
            }
        }
        for(int i=0; i<rows; i++)
        {
            if(board[i][move2]==2)
            {
                board[i][move2]=1;
                break;
            }
        }
        // Now check the third move for the same condition.
        for(int col=0; col<cols; col++)
        {
            if(board[rows-1][col]==2 && col!=move3)
            {
                if(record) {System.out.println("first board, col="+col); printboard();}
                flag=false;
                for(int i=0; i<rows; i++)
                {
                    if(board[i][col]==2)
                    {
                        board[i][col]=1;
                        flag=true;
                        break;
                    }
                }
                assert flag;
                if(record) {System.out.println("After move"); printboard();}
                if(checklines(board)) // If the other move makes a line, then this move3 is not valid.
                {
                    return false;
                }

                // undo the move
                for(int i=0; i<=rows; i++)
                {
                    if(i==rows || board[i][col]==2)
                    {
                        board[i-1][col]=2;
                        break;
                    }
                }
                if(record) {System.out.println("After undo");printboard();}
            }
        }*/

        return true;
    }

    // Old expanded-out fourth move.
    /*  for(int move4=0; move4<cols; move4++)
        {
            if(board[rows-1][move4]==2)  // if this is an allowed move
            {
                if(record) {System.out.println("first board, move4="+move4); printboard();}
                flag=false;
                for(int i=0; i<rows; i++)
                {
                    if(board[i][move4]==2)
                    {
                        board[i][move4]=1;
                        flag=true;
                        break;
                    }
                }
                assert flag;
                if(record) {System.out.println("After move"); printboard();}
                if(checklines(board))
                {
                    return false;
                }

                // undo the move
                for(int i=0; i<=rows; i++)
                {
                    if(i==rows || board[i][move4]==2)
                    {
                        board[i-1][move4]=2;
                        break;
                    }
                }
                if(record) {System.out.println("After undo");printboard();}
            }
        }*/

    // return true for a line.
    boolean checklines(int [][] board)
    {
        // horizontal lines
        for(int row=0; row<rows; row++)
        {
            for(int col=0; col<cols-3; col++) // col=0 for 4x4
            {
                // col is starting column
                if( (board[row][col]==0 && board[row][col+1]==0 && board[row][col+2]==0 && board[row][col+3]==0)
                || (board[row][col]==1 && board[row][col+1]==1 && board[row][col+2]==1 && board[row][col+3]==1))
                    return true;
            }
        }

        // vertical lines
        for(int col=0; col<cols; col++)
        {
            for(int row=0; row<rows-3; row++) // row=0 for 4x4
            {
                // col is starting column
                if( (board[row][col]==0 && board[row+1][col]==0 && board[row+2][col]==0 && board[row+3][col]==0)
                || (board[row][col]==1 && board[row+1][col]==1 && board[row+2][col]==1 && board[row+3][col]==1))
                    return true;
            }
        }

        // up diagonals
        for(int col=0; col<cols-3; col++)
        {
            for(int row=0; row<rows-3; row++)
            {
                // col is starting column
                if( (board[row][col]==0 && board[row+1][col+1]==0 && board[row+2][col+2]==0 && board[row+3][col+3]==0)
                || (board[row][col]==1 && board[row+1][col+1]==1 && board[row+2][col+2]==1 && board[row+3][col+3]==1))
                    return true;
            }
        }

        // down diagonals
        for(int col=0; col<cols-3; col++)
        {
            for(int row=3; row<rows; row++)
            {
                // col is starting column
                if( (board[row][col]==0 && board[row-1][col+1]==0 && board[row-2][col+2]==0 && board[row-3][col+3]==0)
                || (board[row][col]==1 && board[row-1][col+1]==1 && board[row-2][col+2]==1 && board[row-3][col+3]==1))
                    return true;
            }
        }
        return false;
    }

    void printboard()
    {
        for(int row=rows-1; row>=0; row--)
        {
            for(int col=0; col<cols; col++)
            {
                System.out.print(board[row][col]+" | ");
            }
            System.out.println();
        }
    }
}

// V2 The huge constraint. Must use WQGAC for this I think, so that it is loose to begin with
// and doesn't completely cripple the solver.
class c4lookahead5_pred implements predicate_wrapper
{
    c4lookahead5_pred(int rows, int cols)
    {
        this.rows=rows;
        this.cols=cols;

        // allocated here so it is done once only.
        board=new int[rows][];
        for(int row=0; row<rows; row++)
        {
            board[row]=new int[cols];
        }
        this.toreturn=true;
    }

    c4lookahead5_pred(int rows, int cols, boolean toreturn)
    {
        this.rows=rows;
        this.cols=cols;

        // allocated here so it is done once only.
        board=new int[rows][];
        for(int row=0; row<rows; row++)
        {
            board[row]=new int[cols];
        }
        this.toreturn=toreturn;
    }

    final int rows;
    final int cols;
    final int [][] board;
    final boolean toreturn;
    final boolean record=false;
    int numinvocations=0;
    // diagram 1.
    // Two ways to win.

    // board state, 0 equals o and 1=x. 2=unoccupied.
    int counter=0;
    // board[row][column] = state

    public boolean predicate(tuple tau)
    {
        // First copy the first cols*rows vars into the board.
        // board[move][col][row] outside
        //System.out.println("Entered predicate");
        // this board is [row][col]
        //assert tau.vals.length==rows*cols+3;

        for(int col=0; col<cols; col++)
        {
            for(int row=0; row<rows; row++)
            {
                board[row][col]=tau.vals[row+(col*rows)];
            }
        }

        int move1=tau.vals[rows*cols];  // universal
        int move2=tau.vals[rows*cols+1];  // existential.

        if(record){ numinvocations++;System.out.println("invocations:"+numinvocations);}

        if(record)
        {
            printboard();
            System.out.println("move1:"+move1+", move2:"+move2); //, move3:"+move3);
        }

        if(checklines(board)) // this constraint gets out of the way if there is already a line.
        {
            return true;
        }

        // first move, adversary

        boolean flag=false;
        for(int i=0; i<rows; i++)
        {
            if(board[i][move1]==2)
            {
                board[i][move1]=1;   // place a black counter.
                flag=true;
                break;
            }
        }
        if(!flag) return true;

        if(checklines(board))
        {
            return false;
        }

        // move 2

        flag=false;
        for(int i=0; i<rows; i++)
        {
            if(board[i][move2]==2)
            {
                board[i][move2]=0;   // place a red counter.
                flag=true;
                break;
            }
        }
        if(!flag) return false;

        //System.out.println("After first move:");
        //printboard();

        if(checklines(board))
        {
            return true;
        }

        if(record)
        {
            System.out.println("After move 2");
            printboard();
        }

        // expanded out third move.
        for(int move3=0; move3<cols; move3++)
        {
            if(board[rows-1][move3]==2)  // if this is an allowed move
            {
                if(record) {System.out.println("first board, move3="+move3); printboard();}
                flag=false;
                int height=0;
                for(int i=0; i<rows; i++)
                {
                    if(board[i][move3]==2)
                    {
                        board[i][move3]=1;  // place a black counter.
                        flag=true;
                        height=i;
                        break;
                    }
                }
                assert flag;
                if(record) {System.out.println("After move"); printboard();}
                if(checklines(board))
                {
                    return false;
                }

                // undo the move
                board[height][move3]=2;

                if(record) {System.out.println("After undo");printboard();}
            }
        }

        return toreturn;
    }

    // return true for a line.
    boolean checklines(int [][] board)
    {
        // horizontal lines
        for(int row=0; row<rows; row++)
        {
            for(int col=0; col<cols-3; col++) // col=0 for 4x4
            {
                // col is starting column
                if( (board[row][col]==0 && board[row][col+1]==0 && board[row][col+2]==0 && board[row][col+3]==0)
                || (board[row][col]==1 && board[row][col+1]==1 && board[row][col+2]==1 && board[row][col+3]==1))
                    return true;
            }
        }

        // vertical lines
        for(int col=0; col<cols; col++)
        {
            for(int row=0; row<rows-3; row++) // row=0 for 4x4
            {
                // col is starting column
                if( (board[row][col]==0 && board[row+1][col]==0 && board[row+2][col]==0 && board[row+3][col]==0)
                || (board[row][col]==1 && board[row+1][col]==1 && board[row+2][col]==1 && board[row+3][col]==1))
                    return true;
            }
        }

        // up diagonals
        for(int col=0; col<cols-3; col++)
        {
            for(int row=0; row<rows-3; row++)
            {
                // col is starting column
                if( (board[row][col]==0 && board[row+1][col+1]==0 && board[row+2][col+2]==0 && board[row+3][col+3]==0)
                || (board[row][col]==1 && board[row+1][col+1]==1 && board[row+2][col+2]==1 && board[row+3][col+3]==1))
                    return true;
            }
        }

        // down diagonals
        for(int col=0; col<cols-3; col++)
        {
            for(int row=3; row<rows; row++)
            {
                // col is starting column
                if( (board[row][col]==0 && board[row-1][col+1]==0 && board[row-2][col+2]==0 && board[row-3][col+3]==0)
                || (board[row][col]==1 && board[row-1][col+1]==1 && board[row-2][col+2]==1 && board[row-3][col+3]==1))
                    return true;
            }
        }
        return false;
    }

    void printboard()
    {
        for(int row=rows-1; row>=0; row--)
        {
            for(int col=0; col<cols; col++)
            {
                System.out.print(board[row][col]+" | ");
            }
            System.out.println();
        }
    }
}

class c4heuristic implements heuristic
{
    int rows;
    int cols;
    mid_domain [][][] board;

    c4heuristic(int rows, int cols, mid_domain [][][]board)
    {
        this.rows=rows;
        this.cols=cols;
        this.board=board;
    }

    public int choosevalue(variable m)
    {
        // has access to all variable domains through prob.
        mid_domain md1= (mid_domain)m;

        assert !md1.unit();
        // should only be branching on move or a_move variables.
        String name=md1.toString();
        //System.out.println(name.substring(0,4));
        mid_domain [][] boardbefore;
        int move;
        if(name.substring(0,4).equals("move"))
        {
            move=java.lang.Integer.parseInt(name.substring(4));
            if(move==0) return md1.lowerbound();
            boardbefore=board[move-1];
        }
        else
        {
            assert name.substring(0,6).equals("a_move") : name;
            move=java.lang.Integer.parseInt(name.substring(6));
            if(move==0) return md1.lowerbound();
            boardbefore=board[move-1];
        }

        int [][] boardint=new int[rows][];
        for(int row=0; row<rows; row++)
        {
            boardint[row]=new int[cols];
        }

        // boardint[row][col]
        // boardbefore[col][row]

        for(int row=0; row<rows; row++)
        {
            for(int col=0; col<cols; col++)
            {
                boardint[row][col]=boardbefore[col][row].upperbound(); // therefore anything not assigned will be a 2.
            }
        }

        assert !checklines(boardint);
        // no line already.

        // now we have move number and
        // previous board state
        int bestscore=0;
        int bestmove=md1.lowerbound();

        for(int thismove=md1.lowerbound(); thismove<=md1.upperbound(); thismove++)
        {
            if(md1.is_present(thismove))
            {
                // make the move and see if it wins.
                // third move
                boolean flag=false;
                int i = 0;
                for(i=0; i<rows; i++)
                {
                    if(boardint[i][thismove]==2)
                    {
                        boardint[i][thismove]=((move%2)==0)?0:1;  // if ...=0 then it is faster on 4x4x4 !!!
                        flag=true;
                        break;
                    }
                }
                if(!flag)
                {
                    // move is onto a full column.
                    continue;
                }

                if(checklines(boardint))
                {
                    return thismove;  // leftmost winning move.
                }

                int score;
                if(move%2==0)
                {   // red move
                    score=red3lines(boardint);

                }
                else
                {   // black move
                    score=black3lines(boardint);
                }

                if(score>bestscore)
                {
                    bestscore=score;
                    bestmove=thismove;
                }
                // Put the board back. 
                boardint[i][thismove]=2;
            }
        }
        return bestmove;
    }

    int red3lines(int [][]board)
    {
        // horizontal lines
        int lines=0;
        for(int row=0; row<rows; row++)
        {
            for(int col=0; col<cols-3; col++) // col=0 for 4x4
            {
                // col is starting column
                if( check4for3(board[row][col], board[row][col+1], board[row][col+2], board[row][col+3], 0) )
                    lines++;
            }
        }

        // vertical lines
        for(int col=0; col<cols; col++)
        {
            for(int row=0; row<rows-3; row++) // row=0 for 4x4
            {
                // col is starting column
                if( check4for3(board[row][col], board[row+1][col], board[row+2][col], board[row+3][col], 0))
                    lines++;
            }
        }

        // up diagonals
        for(int col=0; col<cols-3; col++)
        {
            for(int row=0; row<rows-3; row++)
            {
                // col is starting column
                if( check4for3(board[row][col], board[row+1][col+1], board[row+2][col+2], board[row+3][col+3], 0))
                    lines++;
            }
        }

        // down diagonals
        for(int col=0; col<cols-3; col++)
        {
            for(int row=3; row<rows; row++)
            {
                // col is starting column
                if( check4for3(board[row][col], board[row-1][col+1], board[row-2][col+2],  board[row-3][col+3], 0))
                    lines++;
            }
        }
        return lines;
    }

    boolean check4for3(int a, int b, int c, int d, int counter) // check a set of 4 if 3 are counter-colour and the other is unset
    {
        if(a==counter && b==counter && c==counter)
        {
            return d==2;  // unset
        }
        else if(a==counter && b==counter && d==counter)
        {
            return c==2;
        }
        else if(a==counter && c==counter && d==counter)
        {
            return b==2;
        }
        else if(b==counter && c==counter && d==counter)
        {
            return a==2;
        }
        else
        {
            return false;
        }
    }

    int black3lines(int [][]board)
    {
        // horizontal lines
        int lines=0;
        for(int row=0; row<rows; row++)
        {
            for(int col=0; col<cols-3; col++) // col=0 for 4x4
            {
                // col is starting column
                if( check4for3(board[row][col], board[row][col+1], board[row][col+2], board[row][col+3], 1) )
                    lines++;
            }
        }

        // vertical lines
        for(int col=0; col<cols; col++)
        {
            for(int row=0; row<rows-3; row++) // row=0 for 4x4
            {
                // col is starting column
                if( check4for3(board[row][col], board[row+1][col], board[row+2][col], board[row+3][col], 1))
                    lines++;
            }
        }

        // up diagonals
        for(int col=0; col<cols-3; col++)
        {
            for(int row=0; row<rows-3; row++)
            {
                // col is starting column
                if( check4for3(board[row][col], board[row+1][col+1], board[row+2][col+2], board[row+3][col+3], 1))
                    lines++;
            }
        }

        // down diagonals
        for(int col=0; col<cols-3; col++)
        {
            for(int row=3; row<rows; row++)
            {
                // col is starting column
                if( check4for3(board[row][col], board[row-1][col+1], board[row-2][col+2],  board[row-3][col+3], 1))
                    lines++;
            }
        }
        return lines;
    }

    /*
    // old one.
    // only does unbroken lines
    int black3lines(int [][]board)
    {
        // horizontal lines
        int lines=0;
        for(int row=0; row<rows; row++)
        {
            for(int col=0; col<cols-2; col++) // col=0 for 4x4
            {
                // col is starting column
                if( (board[row][col]==1 && board[row][col+1]==1 && board[row][col+2]==1) )
                    lines++;
            }
        }

        // vertical lines
        for(int col=0; col<cols; col++)
        {
            for(int row=0; row<rows-2; row++) // row=0 for 4x4
            {
                // col is starting column
                if( (board[row][col]==1 && board[row+1][col]==1 && board[row+2][col]==1 ))
                    lines++;
            }
        }

        // up diagonals
        for(int col=0; col<cols-2; col++)
        {
            for(int row=0; row<rows-2; row++)
            {
                // col is starting column
                if( (board[row][col]==1 && board[row+1][col+1]==1 && board[row+2][col+2]==1))
                    lines++;
            }
        }

        // down diagonals
        for(int col=0; col<cols-2; col++)
        {
            for(int row=2; row<rows; row++)
            {
                // col is starting column
                if( (board[row][col]==1 && board[row-1][col+1]==1 && board[row-2][col+2]==1 ))
                    lines++;
            }
        }
        return lines;
    }*/

    boolean checklines(int [][] board)
    {
        // horizontal lines
        for(int row=0; row<rows; row++)
        {
            for(int col=0; col<cols-3; col++) // col=0 for 4x4
            {
                // col is starting column
                if( (board[row][col]==0 && board[row][col+1]==0 && board[row][col+2]==0 && board[row][col+3]==0)
                || (board[row][col]==1 && board[row][col+1]==1 && board[row][col+2]==1 && board[row][col+3]==1))
                    return true;
            }
        }

        // vertical lines
        for(int col=0; col<cols; col++)
        {
            for(int row=0; row<rows-3; row++) // row=0 for 4x4
            {
                // col is starting column
                if( (board[row][col]==0 && board[row+1][col]==0 && board[row+2][col]==0 && board[row+3][col]==0)
                || (board[row][col]==1 && board[row+1][col]==1 && board[row+2][col]==1 && board[row+3][col]==1))
                    return true;
            }
        }

        // up diagonals
        for(int col=0; col<cols-3; col++)
        {
            for(int row=0; row<rows-3; row++)
            {
                // col is starting column
                if( (board[row][col]==0 && board[row+1][col+1]==0 && board[row+2][col+2]==0 && board[row+3][col+3]==0)
                || (board[row][col]==1 && board[row+1][col+1]==1 && board[row+2][col+2]==1 && board[row+3][col+3]==1))
                    return true;
            }
        }

        // down diagonals
        for(int col=0; col<cols-3; col++)
        {
            for(int row=3; row<rows; row++)
            {
                // col is starting column
                if( (board[row][col]==0 && board[row-1][col+1]==0 && board[row-2][col+2]==0 && board[row-3][col+3]==0)
                || (board[row][col]==1 && board[row-1][col+1]==1 && board[row-2][col+2]==1 && board[row-3][col+3]==1))
                    return true;
            }
        }
        return false;
    }
}
