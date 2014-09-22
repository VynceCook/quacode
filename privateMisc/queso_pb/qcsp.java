package queso.core;

import java.io.*;
import java.util.*;
import gnu.trove.*;
import gnu.math.IntNum;


// this should go away when pure code is sorted out.
import queso.constraints.*;


public class qcsp //implements stateful
{
    // search options
    final boolean checkentailed;
    final boolean sac_preprocess;           // broken
    final boolean jobshop_sac_lookahead;  // specifically for jobshop experiments 
    
    // Statistics
    public long propagations = 0;
    
    // printing options
    final boolean treetrace;
    final boolean trues; 
    final boolean falses;
    final boolean pruning;
    
    // storage options
    final boolean storetree;
    final boolean skeletontree;  // remove nodes for unit variables from the tree.
    final boolean prunefalse; // remove branches which lead to false. i.e. leave only the solution strategy.
    final boolean prunesuboptimal; // with branch and bound, remove branches which lead to suboptimal substrategies.
    
    private final class tree
    {
        tree [] edges;
        variable var;
        boolean trueleaf=false;
    }
    
    tree root;
    
    // should have a getter, not be public.
    public int numnodes;
    
    //int numconstraints=0; // the number of ordinary constraints, not pures
    
    public ArrayList<variable> variables;              // regular variables, not puremonitors
    public ArrayList<constraint> constraints;          // all kinds of constraints
    ArrayList<puremonitor> puremonitors;
    
    // same contents as constraints, but separated into two lists
    ArrayList<constraint> pureconstraints;
    
    // used for encodings.
    public ArrayList<constraint> regularconstraints;
    
    class varblock
    {
        boolean quant;
        ArrayList<variable> vars;
        public String toString()
        {
            return (quant?"forall ":"exists ")+vars;
        }
    }
    
    ArrayList<varblock> blocks;
    
    super_backtrack backtrack;
    
    constraint_queue queue;
    
    public qcsp()
    {
        // default search options
         checkentailed=true;
         sac_preprocess=false;  // currently bugged.
         jobshop_sac_lookahead=false;
         
        // default printing options
         treetrace=false;
         trues=false;
         falses=false;
         pruning=false;

        // default storage options
         storetree=false;
         skeletontree=true;  // remove nodes for unit variables from the tree.
         prunefalse=false; // remove branches which lead to false. i.e. leave only the solution strategy.
         prunesuboptimal=true; // with branch and bound, remove branches which lead to suboptimal substrategies.
        
        queue=new constraint_queue(this);
        backtrack=new super_backtrack(this);
        
        constraints=new ArrayList<constraint>();
        
        pureconstraints=new ArrayList<constraint>();
        regularconstraints=new ArrayList<constraint>();
        
        variables=new ArrayList<variable>();
        
        puremonitors=new ArrayList<puremonitor>();
        
        blocks=new ArrayList<varblock>();
        // initially all constraints are on the queue
        //System.arraycopy(constraints, 0, queue, 0, constraints.length);
        //qindex=constraints.length;
    }
    
    // better constructor
    public qcsp(boolean entailed, boolean sac_pre, boolean printstuff, boolean tree, boolean prunefalses)
    {
        // default search options
         checkentailed=entailed;
         sac_preprocess=sac_pre;  // currently bugged.
         jobshop_sac_lookahead=false;
        // default printing options
         treetrace=printstuff;
         trues=printstuff;
         falses=printstuff;
         pruning=printstuff;

        // default storage options
         storetree=tree;
         skeletontree=true;  // remove nodes for unit variables from the tree.
         prunefalse=prunefalses; // remove branches which lead to false. i.e. leave only the solution strategy.
         prunesuboptimal=true; // with branch and bound, remove branches which lead to suboptimal substrategies.
        
        queue=new constraint_queue(this);
        backtrack=new super_backtrack(this);
        
        constraints=new ArrayList<constraint>();
        
        pureconstraints=new ArrayList<constraint>();
        regularconstraints=new ArrayList<constraint>();
        
        variables=new ArrayList<variable>();
        
        puremonitors=new ArrayList<puremonitor>();
        
        blocks=new ArrayList<varblock>();
        // initially all constraints are on the queue
        //System.arraycopy(constraints, 0, queue, 0, constraints.length);
        //qindex=constraints.length;
    }
    
    public qcsp(boolean entailed, boolean sac_pre, boolean jobshop, boolean trues, boolean falses, boolean tree, boolean prunefalses)
    {
        // default search options
        checkentailed=entailed;
        sac_preprocess=sac_pre;  // currently bugged.
        jobshop_sac_lookahead=jobshop;
        // default printing options
        treetrace=false;
        this.trues=trues;
        this.falses=falses;
        pruning=false;
        
        // default storage options
        storetree=tree;
        skeletontree=true;  // remove nodes for unit variables from the tree.
        prunefalse=prunefalses; // remove branches which lead to false. i.e. leave only the solution strategy.
        prunesuboptimal=true; // with branch and bound, remove branches which lead to suboptimal substrategies.
        
        queue=new constraint_queue(this);
        backtrack=new super_backtrack(this);
        
        constraints=new ArrayList<constraint>();
        
        pureconstraints=new ArrayList<constraint>();
        regularconstraints=new ArrayList<constraint>();
        
        variables=new ArrayList<variable>();
        
        puremonitors=new ArrayList<puremonitor>();
        
        blocks=new ArrayList<varblock>();
        // initially all constraints are on the queue
        //System.arraycopy(constraints, 0, queue, 0, constraints.length);
        //qindex=constraints.length;
    }
    
    // these two should be protected
    public void add_backtrack_level()
    {
        backtrack.add_backtrack_level();
    }
    
    public void backtrack()
    {
        backtrack.backtrack();
    }
    
    // the following two are _only_ to be called from the constructors of
    // constraint and variable. 
    
    // could find some way of making this protected?
    public void addcons(constraint cons)
    {   
        cons.id=constraints.size();
        constraints.add(cons);
        if(cons instanceof stateful)
        {
            backtrack.register_constraint((stateful)cons);
        }
        
        queue.toestablish.add((constraint_iface) cons);
        
        regularconstraints.add(cons);
    }
    
    protected void addvar(variable var)
    {
        if(var instanceof puremonitor)
        {
            var.id=puremonitors.size()+1000000;
            puremonitors.add((puremonitor)var);
            var.blocknumber=1000000;
        }
        else
        {
            var.id=variables.size();   // set id to index in variables.
            variables.add(var);
            
            varblock relevant;
            if(var.id==0)
            {
                relevant=new varblock();
                relevant.quant=var.quant;
                blocks.add(relevant);
                relevant.vars=new ArrayList<variable>();
            }
            else
            {
                varblock temp=blocks.get(blocks.size()-1);
                if(temp.quant==var.quant)
                {
                    relevant=temp;
                }
                else
                {
                    // new block required
                    relevant = new varblock();
                    relevant.quant=var.quant;
                    relevant.vars=new ArrayList<variable>();
                    blocks.add(relevant);
                }
            }
            
            var.blocknumber=blocks.size()-1;
            relevant.vars.add(var);
        }
    }
    
    // has to be called from most constraints.
    public void purecons(constraint cons)
    {
        // this takes a constraint out of regularconstraints
        // and puts it into pureconstraints.
        // a constraint is assumed to be regular unless this is called.
        regularconstraints.remove(cons);
        pureconstraints.add(cons);
    }
    
    public void printdomains()
    {
        System.out.println("Vars and vals:");
        // print all vars and vals
        for(int i=0; i<variables.size(); i++)
        {
            System.out.print(variables.get(i).name+", ");
            System.out.print(variables.get(i).quant?"A":"E");
            System.out.print(", ");
            
            variable v=variables.get(i);
            
            System.out.println(v.domain());
        }
        
        System.out.println("Pure monitors:");
        for(int i=0; i<puremonitors.size(); i++)
        {
            System.out.print(puremonitors.get(i).name+", ");
            System.out.print(puremonitors.get(i).quant);
            System.out.print(", ");
            variable v=puremonitors.get(i);
            
            System.out.println(v.domain());
        }
    }
    
    public void printsol()
    {
        for(int i=0; i<variables.size(); i++)
        {
            variable v=variables.get(i);
            System.out.println((v.quant()?"A ":"E ")+v.name+"="+v.domain());
        }
        System.out.println();
    }
    
    public boolean search()
    {
        numnodes=0;
        
        // sanity check.
        for(int i=0; i<variables.size(); i++)
        {
            if(variables.get(i).empty())
            {
                return false;  // shouldn't this be true if the var is universal??
            }
        }
        
        // ensure constraints all initialized
        if(!establish())
            return false;
        
        if(sac_preprocess)
        {
            boolean res=propagate();
            res=make_sac(blocks.get(0));
            System.out.println("Domains after SAC preprocessing outer block.");
            printdomains();
            
            if(!res) return false;
        }
        
        if(storetree)
        {
            root=new tree();
            return search(0, root);
        }
        else
            return search(0, null);
    }
    
    private boolean search(int currentvar, tree node)
    {
        boolean res=propagate();
        
        if(!res)
        {
            if(falses){System.out.println("False:");this.printdomains();}
            return false;
        }
        
        // no constraint is falsified; check if they are all true.
        if(checkentailed && entailed())   // this might hide quite a lot of work, especially towards the leaf nodes.
        {
            if(treetrace){System.out.println("All constraints entailed, returning true");};
            if(storetree) node.trueleaf=true;
            if(trues){System.out.println("True:");this.printsol();}
            return true;
        }
        
        // instantiate currentvar then recursively call.
        
        // Base case
        if(currentvar==variables.size())
        {
            // We have a full assignment with no conflicts.
            if(trues){System.out.println("True:");this.printsol();}
            // not counting leaf nodes.
            
            if(storetree) node.trueleaf=true;
            
            return true;
        }
        else
        {   // recursive case
            variable v=variables.get(currentvar);
            
            if(storetree)
            {
                node.var=v;
                node.edges=new tree[((mid_domain)v).domsize()];
            }
            
            while(v.unit())  // this is the optimized version but actually makes v.little difference. Makes profiling easier
            {
                if(storetree && !skeletontree)
                {
                    node.var=v;
                    node.edges[((mid_domain)v).lowerbound()]=new tree();  // make a new node and go to it.
                    node=node.edges[((mid_domain)v).lowerbound()];
                }
                currentvar++;
                if(currentvar==variables.size())
                {
                    // We have a full assignment with no conflicts.
                    if(trues){System.out.println("True:");this.printsol();}
                    // not counting leaf nodes.
                    
                    if(storetree) node.trueleaf=true;
                    
                    return true;
                }
                
                v=variables.get(currentvar);
            }
            if(storetree)
            {
                node.var=v;
                node.edges=new tree[((mid_domain)v).domsize()];
            }
            
            // if not unit
            numnodes++;
            
            if(treetrace){System.out.println("Branching on variable "+((v.quant)?"A":"E")+" "+v);}
            
            if(v.quant)
            {
                //universal
                if(v instanceof mid_domain)
                {
                    mid_domain v2=(mid_domain) v;
                    
                    // pick a value
                    while(!v2.empty())  // never actually becomes empty.
                    {
                        // pick a value
                        int value=v2.lowerbound();
                        if(heu!=null && !v2.unit())
                        {
                            value=heu.choosevalue(v);  // use the heuristic if present.
                        }
                        assert v2.is_present(value);
                        
                        // binary branch for value.
                        if(treetrace){System.out.println("Trying "+v+" = "+value);}
                        backtrack.add_backtrack_level();
                        v2.instantiate(value);
                        
                        if(storetree)node.edges[value+v2.offset()]=new tree();
                        
                        boolean result;
                        if(storetree) 
                            result=search(currentvar+1, node.edges[value+v2.offset()]);
                        else
                            result=search(currentvar+1, null);
                        
                        backtrack.backtrack();
                        if(!result)
                        {
                            if(treetrace) System.out.println("Returning false from a universal.");
                            return false;
                        }
                        else
                        {
                            if(v2.unit())  // have just tested the last value.
                            {
                                break;
                            }
                            else
                            {
                                // do the notequal branch.
                                boolean flag=v2.pure(value, null);
                                assert flag;
                                flag=propagate();
                                if(!flag)
                                {
                                    if(treetrace) System.out.println("Returning false from a universal.");
                                    return false;
                                }
                            }
                        }
                    }
                }
                else
                {
                    // fill in here
                    assert false: "attempting to branch on an unbranchable variable:"+v;
                }
                if(treetrace) System.out.println("Returning true from a universal.");
                return true;
            }
            else
            {
                //existential
                if(v instanceof mid_domain)
                {
                    mid_domain v2=(mid_domain) v;
                    
                    // pick a value
                    while(!v2.empty())  // never actually becomes empty.
                    {
                        // pick a value
                        int value=v2.lowerbound();
                        if(heu!=null && !v2.unit())
                        {
                            value=heu.choosevalue(v);  // use the heuristic if present.
                        }
                        assert v2.is_present(value);
                        
                        // binary branch for value.
                        if(treetrace){System.out.println("Trying "+v+" = "+value);}
                        backtrack.add_backtrack_level();
                        v2.instantiate(value);
                        
                        if(storetree)node.edges[value+v2.offset()]=new tree();
                        
                        boolean result;
                        if(storetree) 
                            result=search(currentvar+1, node.edges[value+v2.offset()]);
                        else
                            result=search(currentvar+1, null);
                        
                        backtrack.backtrack();
                        if(result)
                        {
                            if(treetrace) System.out.println("Returning true from a existential.");
                            return true;
                        }
                        else
                        {
                            if(v2.unit())  // have just tested the last value.
                            {
                                break;
                            }
                            else
                            {
                                // do the notequal branch.
                                boolean flag=v2.exclude(value, null);
                                assert flag;
                                flag=propagate();
                                if(!flag)
                                {
                                    if(treetrace) System.out.println("Returning false from a existential.");
                                    return false;
                                }
                            }
                        }
                    }
                }
                else
                {
                    // fill in here
                    assert false: "Attempting to branch on a bounds variable:"+v+" with domain "+v.domain();
                }
                if(treetrace) System.out.println("Returning false from an existential");
                return false;
            }
        }
    }
    
    public boolean bandb(variable_iface maxvar, boolean mean, boolean weightedmean, boolean minimize)
    {
        // maximize variable var
        numnodes=0;
        
        // sanity check.
        for(int i=0; i<variables.size(); i++)
        {
            if(variables.get(i).empty())
            {
                return false;
            }
        }
        
        // ensure constraints all initialized
        if(!establish())
            return false;
        
        if(sac_preprocess)
        {
            boolean res=propagate();
            res=make_sac(blocks.get(0));
            System.out.println("Domains after SAC preprocessing outer block only");
            printdomains();
            
            if(!res) return false;
        }
        
        if(storetree)
        {
            root=new tree();
        }
        else
        {
            root=null;
        }
        
        IntNum bound=bandb(0, root, (mid_domain)maxvar, mean, weightedmean, minimize);
        if(bound.compare(IntNum.minusOne())>0)
            return true;
        else
            return false;
    }
    
    // this version makes a recursive call when the bound is updated, and with relevant values removed
    // like binary branching.
    // new version, re-ported from search procedure. Has weighted mean support
    
    private IntNum bandb(int currentvar, tree node, mid_domain maxvar, boolean mean, boolean weightedmean, boolean minimize)
    {   
        boolean res=propagate();
        
        if(!res)
        {
            if(falses){System.out.println("False:");this.printdomains();}
            return IntNum.minusOne();
        }
        
        // no constraint is falsified; check if they are all true.
        if( currentvar==variables.size() || (checkentailed && entailed()) )   // this might hide quite a lot of work, especially towards the leaf nodes.
        {
            if(treetrace){System.out.println("At leaf node, true.");};
            if(storetree) node.trueleaf=true;
            if(trues){System.out.println("True:");this.printsol();}
            return IntNum.make(maxvar.upperbound());
        }
        else
        {   // recursive case
            variable v=variables.get(currentvar);
            
            if(storetree)
            {
                node.var=v;
                if(v instanceof mid_domain)
                    node.edges=new tree[((mid_domain)v).domsize()];
            }
            
            while(v.unit())  // this is the optimized version but actually makes v.little difference. Makes profiling easier
            {
                if(storetree && !skeletontree)
                {
                    node.var=v;
                    node.edges[((mid_domain)v).lowerbound()]=new tree();  // make a new node and go to it.
                    node=node.edges[((mid_domain)v).lowerbound()];
                }
                currentvar++;
                if(currentvar==variables.size())
                {
                    // We have a full assignment with no conflicts.
                    if(trues){System.out.println("True:");this.printsol();}
                    // not counting leaf nodes.
                    
                    if(storetree) node.trueleaf=true;
                    
                    return IntNum.make(maxvar.upperbound());
                }
                
                v=variables.get(currentvar);
            }
            if(storetree)
            {
                node.var=v;
                node.edges=new tree[((mid_domain)v).domsize()];
            }
            
            // if not unit
            assert !v.unit() && !v.empty();
            numnodes++;
            
            if(treetrace){System.out.println("Branching on variable "+((v.quant)?"A":"E")+" "+v);}
            
            if(v.quant)
            {
                //universal
                IntNum score=mean?IntNum.zero():IntNum.minusOne();  // -1 just indicates no score yet.
                
                if(v instanceof mid_domain)
                {
                    mid_domain v2=(mid_domain) v;
                    
                    // pick a value
                    while(true)
                    {
                        // pick a value
                        int value=v2.lowerbound();
                        if(heu!=null && !v2.unit())
                        {
                            value=heu.choosevalue(v);  // use the heuristic if present.
                        }
                        assert v2.is_present(value);
                        
                        // binary branch for value.
                        if(treetrace){System.out.println("Trying "+v+" = "+value);}
                        backtrack.add_backtrack_level();
                        v2.instantiate(value);
                        
                        if(storetree)node.edges[value+v2.offset()]=new tree();
                        
                        IntNum result=bandb(currentvar+1, storetree?node.edges[value+v2.offset()]:null, maxvar, mean, weightedmean, minimize);
                        
                        backtrack.backtrack();
                        
                        if(mean) //actually just add rather than take the mean
                        {
                            if(weightedmean)
                            {
                                score=IntNum.add(score, IntNum.times(result, ((universal)v2).getweight(value)));
                            }
                            else
                            {
                                score=IntNum.add(score, result);
                            }
                        }
                        else
                        {   
                            int comp=result.compare(score);
                            if(score.compare(IntNum.minusOne())==0 || (!minimize && comp>0) || (minimize && comp<0))
                                // take the minimum, or max if we are minimizing
                                score=result;
                        }
                        
                        if(result.compare(IntNum.minusOne())==0)
                        {
                            if(treetrace) System.out.println("Returning false from a universal.");
                            return result;
                        }
                        else
                        {
                            if(v2.unit())  // have just tested the last value.
                            {
                                break;
                            }
                            else
                            {
                                // do the notequal branch.
                                boolean flag=v2.pure(value, null);
                                assert flag;
                                flag=propagate();
                                if(!flag)
                                {
                                    if(treetrace) System.out.println("Returning false from a universal.");
                                    return IntNum.minusOne();
                                }
                            }
                        }
                    }
                }
                else
                {
                    // fill in here
                    assert false: "attempting to branch on an unbranchable variable:"+v;
                }
                if(treetrace) System.out.println("Returning score "+score+" from a universal.");
                return score;
            }
            else
            {
                //existential
                IntNum score=IntNum.minusOne();  // take the max of the scores and -1.
                if(v instanceof mid_domain)
                {
                    mid_domain v2=(mid_domain) v;
                    
                    // iterate through the values.
                iterate_values:    
                    while(true)
                    {
                        // pick a value
                        int value=v2.lowerbound();
                        if(heu!=null && !v2.unit())
                        {
                            value=heu.choosevalue(v);  // use the heuristic if present.
                        }
                        assert v2.is_present(value);
                        
                        // binary branch for value.
                        if(treetrace){System.out.println("Trying "+v+" = "+value);}
                        backtrack.add_backtrack_level();
                        v2.instantiate(value);
                        
                        if(storetree) node.edges[value+v2.offset()]=new tree();
                        
                        IntNum result=bandb(currentvar+1, storetree?node.edges[value+v2.offset()]:null, maxvar, mean, weightedmean, minimize);
                        
                        backtrack.backtrack();
                        if((result.compare(IntNum.minusOne())!=0) && ( 
                            ((!minimize) && result.compare(score)>0) || 
                            (minimize && result.compare(score)<0) ||
                            (score.compare(IntNum.minusOne())==0)
                            ))
                        {
                            score=result;
                            if(!mean)
                            {   // raise bound of maxvar, or lower if we are minimizing
                                if(treetrace) System.out.println((minimize?"Lowering ":"Raising ")+"bound to "+score+"+/-1 on variable "+v);
                                
                                if(!minimize)
                                {
                                    if(!maxvar.exclude_lower(score.intValue()+1, null))  // score+1 is the new lower bound
                                        break iterate_values;
                                }
                                else
                                {
                                    if(!maxvar.exclude_upper(score.intValue()-1, null))  // score+1 is the new lower bound
                                        break iterate_values;
                                }
                            }
                        }
                        
                        if(v2.unit())  // have just tested the last value.
                        {
                            break iterate_values;
                        }
                        else
                        {
                            // do the notequal branch.
                            boolean flag=v2.exclude(value, null);
                            assert flag;
                            flag=propagate();
                            if(!flag)
                            {
                                break iterate_values;
                            }
                        }
                    }
                }
                else
                {
                    // fill in here
                    assert false: "Attempting to branch on a bounds variable:"+v+" with domain "+v.domain();
                }
                if(treetrace) System.out.println("Returning "+score+" from an existential: "+v);
                return score;
            }
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    // The alternative branch and bound algorithm which returns to the root before bounding and searching again.
    // Called searchOpt in the thesis.
    
    public boolean bandb2(mid_domain maxvar, boolean minimize, mid_domain [] to_bound)
    {
        // maximize variable maxvar
        numnodes=0;
        // sanity check.
        for(int i=0; i<variables.size(); i++)
        {
            if(variables.get(i).empty())
            {
                return false;
            }
        }
        
        // ensure constraints all initialized
        if(!establish())
            return false;
        
        if(sac_preprocess)
        {
            boolean res=propagate();
            res=make_sac(blocks.get(0));
            System.out.println("Domains after SAC preprocessing outer block only");
            printdomains();
            
            if(!res) return false;
        }
        
        if(storetree)
        {
            root=new tree();
        }
        else
        {
            root=null;
        }
        
        add_backtrack_level();
        int bound=bandb2(0, root, (mid_domain)maxvar, minimize);
        backtrack();
        int lastsol=bound;
    branchboundloop:
        while(bound!=-1)
        {
            // change problem to reflect the new bound.
            if(minimize)
            {   // bring the bounds down.
                for(mid_domain m : to_bound)
                {
                    for(int i=m.upperbound(); i>=bound; i--)
                    {
                        if(m.is_present(i) && !m.exclude(i, null))
                        {
                            break branchboundloop;
                        }
                    }
                }
            }
            else
            {
                for(mid_domain m : to_bound)
                {
                    for(int i=m.lowerbound(); i<=bound; i++)
                    {
                        if(m.is_present(i) && !m.exclude(i, null))
                        {
                            break branchboundloop;
                        }
                    }
                }
            }
            System.out.println("Successfully reduced bound to:"+bound);
            lastsol=bound;
            
            add_backtrack_level();
            if(storetree) root=new tree();
            bound=bandb2(0, root, (mid_domain)maxvar, minimize);
            backtrack();
        }
        
        if(lastsol!=-1)
        {
            System.out.println("Score:"+lastsol);
            return true;
        }
        else
            return false;
    }
    
    private int bandb2(int currentvar, tree node, mid_domain maxvar, boolean minimize)
    {
        boolean res=propagate();
        
        // added bit here for jobshop lookahead
        if(jobshop_sac_lookahead && res)
        {
            res=jobshop_lookahead(currentvar);
            if(!res)
            {
                //System.out.println("Won here with jobshop lookahead");   
            }
        }
        
        if(!res)
        {
            if(falses){System.out.println("False:");this.printdomains();}
            return -1;
        }
        
        // no constraint is falsified; check if they are all true.
        if(checkentailed && entailed())   // this might hide quite a lot of work, especially towards the leaf nodes.
        {
            if(treetrace){System.out.println("All constraints entailed, returning true");};
            if(storetree) node.trueleaf=true;
            if(trues){System.out.println("True:");this.printsol();}
            return minimize?(maxvar.lowerbound()):(maxvar.upperbound());
        }
        
        // instantiate currentvar then recursively call.
        
        // Base case
        if(currentvar==variables.size())
        {
            // We have a full assignment with no conflicts.
            if(trues){System.out.println("True:");this.printsol();}
            // not counting leaf nodes.
            
            if(storetree) node.trueleaf=true;
            
            return minimize?(maxvar.lowerbound()):(maxvar.upperbound());
        }
        else
        {   // recursive case
            variable v=variables.get(currentvar);
            
            if(storetree)
            {
                node.var=v;
                node.edges=new tree[((mid_domain)v).domsize()];
            }
            
            while(v.unit())  // this is the optimized version but actually makes v.little difference. Makes profiling easier
            {
                if(storetree && !skeletontree)
                {
                    node.var=v;
                    node.edges[((mid_domain)v).lowerbound()]=new tree();  // make a new node and go to it.
                    node=node.edges[((mid_domain)v).lowerbound()];
                }
                currentvar++;
                if(currentvar==variables.size())
                {
                    // We have a full assignment with no conflicts.
                    if(trues){System.out.println("True:");this.printsol();}
                    // not counting leaf nodes.
                    
                    if(storetree) node.trueleaf=true;
                    
                    return minimize?(maxvar.lowerbound()):(maxvar.upperbound());
                }
                
                v=variables.get(currentvar);
            }
            if(storetree)
            {
                node.var=v;
                node.edges=new tree[((mid_domain)v).domsize()];
            }
            
            // if not unit
            numnodes++;
            
            if(treetrace){System.out.println("Branching on variable "+((v.quant)?"A":"E")+" "+v);}
            
            if(v.quant)
            {
                //universal
                int score=-1;
                if(v instanceof mid_domain)
                {
                    mid_domain v2=(mid_domain) v;
                    
                    // pick a value
                    while(!v2.empty())  // never actually becomes empty.
                    {
                        // pick a value
                        int value=v2.lowerbound();
                        if(heu!=null && !v2.unit())
                        {
                            value=heu.choosevalue(v);  // use the heuristic if present.
                        }
                        assert v2.is_present(value);
                        
                        // binary branch for value.
                        if(treetrace){System.out.println("Trying "+v+" = "+value);}
                        backtrack.add_backtrack_level();
                        v2.instantiate(value);
                        
                        if(storetree)node.edges[value+v2.offset()]=new tree();
                        
                        int result;
                        if(storetree) 
                            result=bandb2(currentvar+1, node.edges[value+v2.offset()], maxvar, minimize);
                        else
                            result=bandb2(currentvar+1, null, maxvar, minimize);
                        
                        backtrack.backtrack();
                        
                        if(score==-1 || (!minimize && result<score) || (minimize && result>score))
                        {
                            // take the minimum, or max if we are minimizing
                            score=result;
                        }
                        
                        if(result==-1)
                        {
                            if(treetrace) System.out.println("Returning false from a universal.");
                            return -1;
                        }
                        else
                        {
                            if(v2.unit())  // have just tested the last value.
                            {
                                break;
                            }
                            else
                            {
                                // do the notequal branch.
                                boolean flag=v2.pure(value, null);
                                assert flag;
                                flag=propagate();
                                if(!flag)
                                {
                                    if(treetrace) System.out.println("Returning false from a universal.");
                                    return -1;
                                }
                            }
                        }
                    }
                }
                else
                {
                    // fill in here
                    assert false: "attempting to branch on an unbranchable variable:"+v;
                }
                if(treetrace) System.out.println("Returning true from a universal.");
                return score;
            }
            else
            {
                //existential
                if(v instanceof mid_domain)
                {
                    mid_domain v2=(mid_domain) v;
                    
                    // pick a value
                    while(!v2.empty())  // never actually becomes empty.
                    {
                        // pick a value
                        int value=v2.lowerbound();
                        if(heu!=null && !v2.unit())
                        {
                            value=heu.choosevalue(v);  // use the heuristic if present.
                        }
                        assert v2.is_present(value);
                        
                        // binary branch for value.
                        if(treetrace){System.out.println("Trying "+v+" = "+value);}
                        backtrack.add_backtrack_level();
                        v2.instantiate(value);
                        
                        if(storetree)node.edges[value+v2.offset()]=new tree();
                        
                        int result;
                        if(storetree)
                            result=bandb2(currentvar+1, node.edges[value+v2.offset()], maxvar, minimize);
                        else
                            result=bandb2(currentvar+1, null, maxvar, minimize);
                        
                        backtrack.backtrack();
                        if(result!=-1)
                        {
                            if(treetrace) System.out.println("Returning true from a existential.");
                            return result;
                        }
                        else
                        {
                            if(v2.unit())  // have just tested the last value.
                            {
                                break;
                            }
                            else
                            {
                                // do the notequal branch.
                                boolean flag=v2.exclude(value, null);
                                assert flag;
                                flag=propagate();
                                if(!flag)
                                {
                                    if(treetrace) System.out.println("Returning false from a existential.");
                                    return -1;
                                }
                                if(v2.is_functional())
                                {
                                    // the variable is a functional one, so 
                                    if(treetrace) System.out.println("Tried one value for a functional variable; now returning false.");
                                    return -1;
                                }
                            }
                        }
                    }
                }
                else
                {
                    // fill in here
                    assert false: "Attempting to branch on a bounds variable:"+v+" with domain "+v.domain();
                }
                if(treetrace) System.out.println("Returning false from an existential");
                return -1;
            }
        }
    }
    
    
    private boolean jobshop_lookahead(int v1)
    {
        // v1 is the variable that we are just about to branch on. If it is a universal,
        // just return because we will branch for 1 anyway.
        assert v1>=0 && v1<variables.size();
        //if(variables.get(v1).quant()) return true;
        
        int u=v1;  // u will be the next universal.
        while(u<variables.size() && ( !variables.get(u).quant() || 
             !((mid_domain)variables.get(u)).is_present(1) || !((mid_domain)variables.get(u)).is_present(0) ) )
        {
            u++;
        }
        if(u==variables.size())
        {
            return true;
        }
        
        mid_domain uni= (mid_domain) variables.get(u);
        
        add_backtrack_level();
        
        // set u to 1 and propagate
        uni.instantiate(1);
        boolean res=propagate();
        
        backtrack();
        
        return res;
    }
    
    // super strong version.
    private boolean jobshop_lookahead2(int v1)
    {
        // v1 is the variable that we are just about to branch on. If it is a universal,
        // just return because we will branch for 1 anyway.
        assert v1>=0 && v1<variables.size();
        //if(variables.get(v1).quant()) return true;
        
        int u=v1;  // u will be the next universal.
        
        add_backtrack_level();
        while(true)
        {
            while(u<variables.size() && ( !variables.get(u).quant() || 
                !((mid_domain)variables.get(u)).is_present(1) || !((mid_domain)variables.get(u)).is_present(0) ) )
            {
                u++;
            }
            if(u==variables.size())
            {
                backtrack();
                return true;
            }
            mid_domain uni= (mid_domain) variables.get(u);
            
            add_backtrack_level();
            
                // set u to 1 and propagate
                uni.instantiate(1);
                boolean res=propagate();
            
            backtrack();
            
            if(!res) 
            {
                backtrack();
                return false;
            }
            
            // set u to 0 and continue to the next uni variable.
            uni.instantiate(0);
        }
        
    }
    
    // maxvar can currently only be an fd_var and is expected to be existential 
    /*private int bandb(int currentvar, tree node, variable_iface maxvar)  // the return value is the bound, -1 for failed.
    {
        // if maxvar.upperbound drops below bound, then we cannot beat the already found solution
        // so we return false.
        
        boolean res=propagate();
        
        if(!res)
            return -1;
        
        // instantiate currentvar then recursively call.
        
        // Base case
        if(currentvar==variables.size())
        {
            // We have a full assignment with no conflicts.
            if(trues){System.out.println("True:");this.printsol();}
            // not counting leaf nodes.
            
            node.trueleaf=true;
            
            assert maxvar.unit();
            return ((mid_domain)maxvar).lowerbound();
        }
        else
        {   // recursive case
            variable curvar=variables.get(currentvar);
            
            if(storetree)
            {
                // tree storage. only works for fd variables
                node.var=curvar;
                
                
                // optimization for unit variables:
                if(curvar.unit())
                {
                    if(skeletontree)
                    {
                        return bandb(currentvar+1, node, maxvar);
                    }
                    else
                    {
                        node.edges=new tree[((mid_domain)curvar).domsize()];
                        node.edges[((mid_domain)curvar).lowerbound()]=new tree();
                        return bandb(currentvar+1, node.edges[((mid_domain)curvar).lowerbound()], maxvar);
                    }
                }
            }
            // if not unit
            numnodes++;
            
            if(treetrace){System.out.println("Branching on variable "+curvar+" (universal: "+curvar.quant+")");}
            
            if(curvar.quant)
            {
                //universal
                
                int min=1000000;
                // also may have to branch for a pure value. NO because universal variable has no effect
                // in this case so it cannot have a lower score than some other value.
                if(curvar instanceof mid_domain)
                {
                    for(int i=((mid_domain)curvar).lowerbound(); i<=((mid_domain)curvar).upperbound(); i++)
                    {
                        if(((mid_domain)curvar).is_present(i))
                        {
                            if(treetrace){System.out.println("Trying "+curvar+" = "+i);}
                            backtrack.add_backtrack_level();
                                ((mid_domain)curvar).instantiate(i);
                                
                                if(storetree) node.edges[i]=new tree();
                                
                                int result=bandb(currentvar+1, (node==null)?null:node.edges[i], maxvar);
                            backtrack.backtrack();
                            if(result==-1)
                            {
                                if(treetrace) System.out.println("Returning false from a universal.");
                                return -1;
                            }
                            
                            if(result<min)
                            {
                                min=result;
                            }
                        }
                    }
                }
                else
                {
                    // fill in here
                    assert false: "Tried to branch on a bounds variable.";
                }
                if(treetrace) System.out.println("Returning "+min+" from a universal.");
                return min;
            }
            else
            {
                //existential -- asymmetric with universal.
                int max=-1;
                
                if(curvar instanceof mid_domain)
                {
                    for(int i=((mid_domain)curvar).lowerbound(); i<=((mid_domain)curvar).upperbound(); i++)
                    {
                        if(((mid_domain)curvar).is_present(i))
                        {
                            if(treetrace){System.out.println("Trying "+curvar+" = "+i);}
                            backtrack.add_backtrack_level();
                                ((mid_domain)curvar).instantiate(i);

                                if(storetree) node.edges[i]=new tree();
                                int result=bandb(currentvar+1, (node==null)?null:node.edges[i], maxvar);

                                if(result==-1 && prunefalse && storetree) node.edges[i]=null;  // cut off the branch that leads to false.
                            backtrack.backtrack();
                            
                            if(result>max)
                            {
                                // raise the crossbar
                                // bug: this should probably be implemented as a bounds operation.
                                for(int j=max; j<=result; j++)
                                {
                                    // prune value j
                                    if(((mid_domain)maxvar).is_present(j))
                                        ((mid_domain)maxvar).exclude(j, null);   // this gets propagated on the recursive call.
                                }

                                // could do optimization here --- early detection of the failure of the rest of the values.
                                // i.e. throw away the tried values of curvar and propagate, see what happens.

                                max=result;
                                if(prunesuboptimal)
                                {   
                                    // throw away suboptimal branches
                                    for(int j=i-1; j>=0; j--)  //lousy way of doing it. There's only one.
                                    {
                                        node.edges[j]=null;
                                    }
                                }
                            }
                        }
                    }
                }
                else
                {
                    // fill in
                    assert false: "Tried to branch on a bounds variable.";
                }
                
                if(max>-1)
                {
                    if(treetrace) System.out.println("Returning "+max+" from an existential.");
                    return max;
                }
                
                if(treetrace) System.out.println("Returning false from an existential");
                return -1;
            }
        }
    }*/
    
    // these should be called rather than directly calling any methods on constraints or queue objects.
    
    // it would be nice if these two were protected, would require a generic test harness in core.
    public boolean propagate()
    {
        return queue.process_all();
    }
    
    public boolean establish()
    {
        return queue.establish();
    }
    
    constraint entail_watch=null;
    
    private boolean entailed()
    {
        // check if all constraints are entailed.
        // The watch is one that is not entailed.
        if(entail_watch!=null)
        {
            if(!entail_watch.entailed())
            {
                //System.out.println("Constraint not entailed:"+entail_watch);
                //System.out.println("Watch level 1 effective");
                return false;
            }
        }
        
        // otherwise search for a new watch
        for(int i=regularconstraints.size()-1; i>=0; i--)
        {
            constraint cons=regularconstraints.get(i);
            if(!cons.entailed())
            {
                entail_watch=cons;
                //System.out.println("Constraint not entailed:"+entail_watch);
                return false;
            }
        }
        // searched all constraints and they are all entailed.
        return true;
    }
    
    
    
    ////////////////////////////////////////////////////////////////
    // Pure value rule setup
    
    public void pure_setup()
    {
        // iterate through the constraints, constructing inverses and
        // maintaining an arraylist of copies (puremonitors) of each problem variable
        
        // must be called after everything else is set up.
        
        // local set of puremonitors is called pm, problem set is called puremonitors.
        
        ArrayList<ArrayList<puremonitor>> pm=new ArrayList<ArrayList<puremonitor>>();
        for(int i=0; i<variables.size(); i++)
            pm.add(new ArrayList<puremonitor>());
        
        // iterate through constraints
        int csize=constraints.size(); // This will change during the iteration, so store it here.
        for(int i=0; i<csize; i++)
        {
            constraint c1=constraints.get(i);
            
            if(c1 instanceof gac_schema_predicate)
            {
                gac_schema_predicate s1 = (gac_schema_predicate) c1;
                predicate_wrapper p1=s1.pred;
                predicate_wrapper p2=new negated_predicate(p1);
                puremonitor [] vars=new puremonitor[s1.variables().length];
                for(int j=0; j<s1.variables().length; j++)
                {
                    // doesn't matter that the original has holes in the domain, because ????
                    vars[j]=new puremonitor(((mid_domain)s1.variables()[j]).lowerbound(), ((mid_domain)s1.variables()[j]).upperbound(), this, "pm-c"+i+"-"+s1.variables()[j]);
                    pm.get(s1.variables()[j].id()).add(vars[j]);
                }
                gac_schema_predicate c2=new gac_schema_predicate(vars, this, p2);
                purecons(c2);
            }
            else if(c1 instanceof gac_schema_positive)
            {
                gac_schema_positive s1 = (gac_schema_positive) c1;
                predicate_wrapper p1=s1.pred;
                predicate_wrapper p2=new negated_predicate(p1);
                puremonitor [] vars=new puremonitor[s1.variables().length];
                for(int j=0; j<s1.variables().length; j++)
                {
                    // doesn't matter that the original has holes in the domain, because ????
                    vars[j]=new puremonitor(((mid_domain)s1.variables()[j]).lowerbound(), ((mid_domain)s1.variables()[j]).upperbound(), this, "pm-c"+i+"-"+s1.variables()[j]);
                    pm.get(s1.variables()[j].id()).add(vars[j]);
                }
                gac_schema_positive c2=new gac_schema_positive(vars, this, p2);
                purecons(c2);
            }
            else if(c1 instanceof sqgac)
            {
                sqgac s1 = (sqgac) c1;
                predicate_wrapper p1=s1.pred;
                predicate_wrapper p2=new negated_predicate(p1);
                puremonitor [] vars=new puremonitor[s1.variables().length];
                for(int j=0; j<s1.variables().length; j++)
                {
                    // doesn't matter that the original has holes in the domain, because ????
                    vars[j]=new puremonitor(((mid_domain)s1.variables()[j]).lowerbound(), ((mid_domain)s1.variables()[j]).upperbound(), this, "pm-c"+i+"-"+s1.variables()[j]);
                    pm.get(s1.variables()[j].id()).add(vars[j]);
                }
                sqgac c2=new sqgac_nofail(vars, this, p2);
                purecons(c2);
            }
            else if(c1 instanceof nightingaletuples)
            {
                nightingaletuples s1 = (nightingaletuples) c1;
                predicate_wrapper p1=s1.pred;
                predicate_wrapper p2=new negated_predicate(p1);
                puremonitor [] vars=new puremonitor[s1.variables().length];
                for(int j=0; j<s1.variables().length; j++)
                {
                    // doesn't matter that the original has holes in the domain, because ????
                    vars[j]=new puremonitor(((mid_domain)s1.variables()[j]).lowerbound(), ((mid_domain)s1.variables()[j]).upperbound(), this, "pm-c"+i+"-"+s1.variables()[j]);
                    pm.get(s1.variables()[j].id()).add(vars[j]);
                }
                nightingaletuples c2=new nightingaletuples(vars, this, p2, s1.listperliteral, s1.hologramlist);
                purecons(c2);
            }
            
            else if(c1 instanceof or_constraint)
            {
                // just need to flip the negation for the xi variable.
                or_constraint s1 = (or_constraint) c1;
                boolean[] neg=new boolean[s1.negated.length];
                System.arraycopy(s1.negated, 0, neg, 0, s1.negated.length);
                neg[s1.xi]=!neg[s1.xi];
                
                puremonitor [] vars=new puremonitor[s1.variables().length];
                for(int j=0; j<s1.variables().length; j++)
                {
                    // doesn't matter that the original has holes in the domain, because ????
                    vars[j]=new puremonitor(((mid_domain)s1.variables()[j]).lowerbound(), ((mid_domain)s1.variables()[j]).upperbound(), this, "pm-c"+i+"-"+s1.variables()[j]);
                    pm.get(s1.variables()[j].id()).add(vars[j]);
                }
                or_constraint c2=new or_constraint_nofail(vars, neg, s1.xi, this);
                purecons(c2);
            }
            else
            {
                assert false : "could not negate the constraint.";  // actually could easily handle all the constraints.
            }
        }
        
        // now make the linkups
        for(int i=0; i<variables.size(); i++)
        {
            ArrayList<puremonitor> temp=pm.get(i);
            // make a pure linkup with temp.
            mid_domain[] vs= new mid_domain[temp.size()+1];
            
            vs[0]=(mid_domain)variables.get(i);  // original variable
            for(int j=0; j<temp.size(); j++)
                vs[j+1]=temp.get(j);
            
            linkup_pures c2=new linkup_pures(vs, this);
            purecons(c2);
        }
    }
    
    /*
    //perh. replace pure_Setup_uuniversals with pure_setup_list
    then use this.
    
    public void pure_setup_universals()
    {
        ArrayList<variable> universals= new ArrayList<variable>();
        for(int i=0; i<variables.size(); i++)
        {
            if(variables.get(i).quant())
            {
                universals.add(variables.get(i));
            }
        }
        pure_setup_list(universals);
    }*/
    
    // pure value rule for universals only.
    public void pure_setup_universals()
    {
        // this is trickier. For each universal variable, search the constraints.
        int csize=constraints.size();
        
        for(int i=0; i<variables.size(); i++)
        {
            if(variables.get(i).quant())
            {
                mid_domain v1=(mid_domain)variables.get(i);
                
                ArrayList<constraint> clist=new ArrayList<constraint>();
                TIntArrayList v1index=new TIntArrayList();
                for(int j=0; j<csize; j++)
                {
                    variable_iface [] cvars;
                    constraint_iface c1=(constraint_iface)constraints.get(j);
                    cvars=c1.variables();
                    
                    for(int k=0; k<cvars.length; k++) 
                    {
                        if(cvars[k]==v1)
                        {
                            clist.add(constraints.get(j));
                            v1index.add(k);
                            break;
                        }
                    }
                }
                // now clist contains all the constraints with v1 in them.
                
                puremonitor[] pm1=new puremonitor[clist.size()]; // this is the list of puremonitors to be linked up to v1
                
                for(int cindex=0; cindex<clist.size(); cindex++)
                {
                    pm1[cindex]=new puremonitor(v1.lowerbound(), v1.upperbound(), this, "pm-c"+cindex+"-"+v1);
                    constraint c1=clist.get(cindex);
                    int v1ind=v1index.get(cindex);
                    
                    if(c1 instanceof negatable)
                    {
                        ((negatable)c1).make_negated_constraint(pm1[cindex], v1ind); // pass in a single pure monitor variable and its index
                                            //; all the rest will be dummy linked-up.
                    }
                    /*
                    else if(c1 instanceof gac_schema_predicate)
                    {
                        gac_schema_predicate s1 = (gac_schema_predicate) c1;
                        predicate_wrapper p1=s1.pred;
                        predicate_wrapper p2=new negated_predicate(p1);
                        puremonitor [] vars=new puremonitor[s1.variables().length];
                        
                        for(int j=0; j<s1.variables().length; j++)
                        {
                            // doesn't matter that the original has holes in the domain, because ????
                            if(j==v1ind) vars[j]=pm1[cindex];
                            else
                            {
                                vars[j]=new puremonitor(((mid_domain)s1.variables()[j]).lowerbound(), ((mid_domain)s1.variables()[j]).upperbound(), this, "pm-c"+i+"-"+s1.variables()[j]);
                                // make the dummy linkup.
                                mid_domain[] dlvars={((mid_domain)s1.variables()[j]), vars[j]};
                                constraint dl1=new dummy_linkup(dlvars, this);
                                purecons(dl1);
                            }
                        }
                        gac_schema_predicate c2=new gac_schema_predicate_nofail(vars, this, p2);
                        purecons(c2);
                    }*/
                    else if(c1 instanceof nightingaletuples)
                    {
                        nightingaletuples s1 = (nightingaletuples) c1;
                        predicate_wrapper p1=s1.pred;
                        predicate_wrapper p2=new negated_predicate(p1);
                        puremonitor [] vars=new puremonitor[s1.variables().length];
                        
                        for(int j=0; j<s1.variables().length; j++)
                        {
                            // doesn't matter that the original has holes in the domain, because ????
                            if(j==v1ind) vars[j]=pm1[cindex];
                            else
                            {
                                vars[j]=new puremonitor(((mid_domain)s1.variables()[j]).lowerbound(), ((mid_domain)s1.variables()[j]).upperbound(), this, "pm-c"+i+"-"+s1.variables()[j]);
                                // make the dummy linkup.
                                mid_domain[] dlvars={((mid_domain)s1.variables()[j]), vars[j]};
                                constraint dl1=new dummy_linkup(dlvars, this);
                                purecons(dl1);
                            }
                        }
                        System.out.println("Making new constraint for pure value over "+Arrays.asList(vars));
                        constraint c2=new nightingaletuples_nofail(vars, this, p2, s1.listperliteral, s1.hologramlist);
                        purecons(c2);
                    }
                    /*else if(c1 instanceof gac_schema_positive)
                    {
                        gac_schema_positive s1 = (gac_schema_positive) c1;
                        predicate_wrapper p1=s1.pred;
                        predicate_wrapper p2=new negated_predicate(p1);
                        puremonitor [] vars=new puremonitor[s1.variables().length];
                        
                        for(int j=0; j<s1.variables().length; j++)
                        {
                            // doesn't matter that the original has holes in the domain, because ????
                            if(j==v1ind) vars[j]=pm1[cindex];
                            else
                            {
                                vars[j]=new puremonitor(((mid_domain)s1.variables()[j]).lowerbound(), ((mid_domain)s1.variables()[j]).upperbound(), this, "pm-c"+i+"-"+s1.variables()[j]);
                                // make the dummy linkup.
                                mid_domain[] dlvars={((mid_domain)s1.variables()[j]), vars[j]};
                                constraint dl1=new dummy_linkup(dlvars, this);
                                purecons(dl1);
                            }
                        }
                        System.out.println("Making new constraint for pure value over "+Arrays.asList(vars));
                        constraint c2=new nightingaletuples_nofail(vars, this, p2);
                        purecons(c2);
                    }*/
                    else if(c1 instanceof table_constraint)
                    {
                        table_constraint s1 = (table_constraint) c1;
                        predicate_wrapper p1=s1.pred;
                        predicate_wrapper p2=new negated_predicate(p1);
                        puremonitor [] vars=new puremonitor[s1.variables().length];
                        
                        for(int j=0; j<s1.variables().length; j++)
                        {
                            // doesn't matter that the original has holes in the domain, because ????
                            if(j==v1ind) vars[j]=pm1[cindex];
                            else
                            {
                                vars[j]=new puremonitor(((mid_domain)s1.variables()[j]).lowerbound(), ((mid_domain)s1.variables()[j]).upperbound(), this, "pm-c"+i+"-"+s1.variables()[j]);
                                // make the dummy linkup.
                                mid_domain[] dlvars={((mid_domain)s1.variables()[j]), vars[j]};
                                constraint dl1=new dummy_linkup(dlvars, this);
                                purecons(dl1);
                            }
                        }
                        System.out.println("Making new constraint for pure value over "+Arrays.asList(vars));
                        constraint c2=new table_constraint(vars, this, p2);
                        purecons(c2);
                    }
                    else if(c1 instanceof sqgac)
                    {
                        sqgac s1 = (sqgac) c1;
                        predicate_wrapper p1=s1.pred;
                        predicate_wrapper p2=new negated_predicate(p1);
                        puremonitor [] vars=new puremonitor[s1.variables().length];
                        
                        for(int j=0; j<s1.variables().length; j++)
                        {
                            // doesn't matter that the original has holes in the domain, because ????
                            if(j==v1ind) vars[j]=pm1[cindex];
                            else
                            {
                                vars[j]=new puremonitor(((mid_domain)s1.variables()[j]).lowerbound(), ((mid_domain)s1.variables()[j]).upperbound(), this, "pm-c"+i+"-"+s1.variables()[j]);
                                // make the dummy linkup.
                                mid_domain[] dlvars={((mid_domain)s1.variables()[j]), vars[j]};
                                constraint dl1=new dummy_linkup(dlvars, this);
                                purecons(dl1);
                            }
                        }
                        sqgac c2=new sqgac_nofail(vars, this, p2);
                        purecons(c2);
                    }
                    else if(c1 instanceof or_constraint)
                    {
                        // just need to flip the negation for the xi variable.
                        or_constraint s1 = (or_constraint) c1;
                        boolean[] neg=new boolean[s1.negated.length];
                        System.arraycopy(s1.negated, 0, neg, 0, s1.negated.length);
                        neg[s1.xi]=!neg[s1.xi];
                        
                        puremonitor [] vars=new puremonitor[s1.variables().length];
                        
                        for(int j=0; j<s1.variables().length; j++)
                        {
                            // doesn't matter that the original has holes in the domain, because ????
                            if(j==v1ind) vars[j]=pm1[cindex];
                            else
                            {
                                vars[j]=new puremonitor(((mid_domain)s1.variables()[j]).lowerbound(), ((mid_domain)s1.variables()[j]).upperbound(), this, "pm-c"+i+"-"+s1.variables()[j]);
                                // make the dummy linkup.
                                mid_domain[] dlvars={((mid_domain)s1.variables()[j]), vars[j]};
                                constraint dl1=new dummy_linkup(dlvars, this);
                                purecons(dl1);
                            }
                        }
                        or_constraint c2=new or_constraint_nofail(vars, neg, s1.xi, this);
                        purecons(c2);
                    }
                    else if(c1 instanceof or_constraint_values)
                    {
                        // just need to flip the negation for the xi variable.
                        or_constraint_values s1 = (or_constraint_values) c1;
                        boolean[] neg=new boolean[s1.negated.length];
                        System.arraycopy(s1.negated, 0, neg, 0, s1.negated.length);
                        neg[s1.xi]=!neg[s1.xi];
                        
                        puremonitor [] vars=new puremonitor[s1.variables().length];
                        
                        for(int j=0; j<s1.variables().length; j++)
                        {
                            // doesn't matter that the original has holes in the domain, because ????
                            if(j==v1ind) vars[j]=pm1[cindex];
                            else
                            {
                                vars[j]=new puremonitor(((mid_domain)s1.variables()[j]).lowerbound(), ((mid_domain)s1.variables()[j]).upperbound(), this, "pm-c"+i+"-"+s1.variables()[j]);
                                // make the dummy linkup.
                                mid_domain[] dlvars={((mid_domain)s1.variables()[j]), vars[j]};
                                constraint dl1=new dummy_linkup(dlvars, this);
                                purecons(dl1);
                            }
                        }
                        or_constraint_values c2=new or_constraint_values_nofail(vars, s1.values, neg, s1.xi, this);
                        purecons(c2);
                    }
                    else if(c1 instanceof or_constraint_comparison)
                    {
                        // just need to flip the operator for the xi variable.
                        or_constraint_comparison s1 = (or_constraint_comparison) c1;
                        int[] values=new int[s1.values.length];
                        System.arraycopy(s1.values, 0, values, 0, s1.values.length);
                        int[] comp=new int[s1.comparisons.length];
                        System.arraycopy(s1.comparisons, 0, comp, 0, s1.comparisons.length);
                        int xi=s1.xi;
                        if(comp[xi]==-1)
                        {
                            comp[xi]=1; values[xi]--;
                        }
                        else if(comp[xi]==1)
                        {
                            comp[xi]=-1; values[xi]++;
                        }
                        else if(comp[xi]==0)
                            comp[xi]=2;
                        else if(comp[xi]==2)
                            comp[xi]=0;
                        
                        puremonitor [] vars=new puremonitor[s1.variables().length];
                        
                        for(int j=0; j<s1.variables().length; j++)
                        {
                            // doesn't matter that the original has holes in the domain, because ????
                            if(j==v1ind) vars[j]=pm1[cindex];
                            else
                            {
                                vars[j]=new puremonitor(((mid_domain)s1.variables()[j]).lowerbound(), ((mid_domain)s1.variables()[j]).upperbound(), this, "pm-c"+i+"-"+s1.variables()[j]);
                                // make the dummy linkup.
                                mid_domain[] dlvars={((mid_domain)s1.variables()[j]), vars[j]};
                                constraint dl1=new dummy_linkup(dlvars, this);
                                purecons(dl1);
                            }
                        }
                        or_constraint_comparison c2=new or_constraint_comparison_nofail(vars, values, comp, s1.xi, this);
                        purecons(c2);
                    }
                    else
                    {
                        assert false : "could not negate the constraint.";  // actually could easily handle all the constraints.
                    }
                }
                
                // Now create the linkup_pures with v1 and pm1
                mid_domain[] lpvars=new mid_domain[pm1.length+1];
                lpvars[0]=v1;
                System.arraycopy(pm1, 0, lpvars, 1, pm1.length);
                System.out.println(Arrays.asList(lpvars));
                constraint lp1=new linkup_pures(lpvars, this);
                purecons(lp1);
            }
        }
    }
    
    private boolean make_sac(varblock vb)
    {
        // Probably impossible to do it for all variables, because
        // instantiating an inner existential could prune an outer universal
        // and cause failure. 
        // Possible to do it for the outermost block at any time though I think.
        
        // Optimal (time) version duplicates the problem.
        // This is a really naive version.
        variable var;
        boolean sweep=true;
        
        while(sweep)
        {
            sweep=false;
            
            for(int i=0; i<vb.vars.size(); i++)
            {
                var=vb.vars.get(i);
                
                if(var instanceof mid_domain)
                {
                    for(int j=((mid_domain)var).lowerbound(); j<=((mid_domain)var).upperbound(); j++)
                    {
                        if(((mid_domain)var).is_present(j))
                        {
                            boolean res=sactest(((mid_domain)var), j);
                            if(!res)
                            {   
                                System.out.println("Singleton consistency removing "+var+" : "+j);
                                res=((mid_domain)var).exclude(j, null);
                                if(!res) return false;
                                res=propagate();   // this may be paranoia, but at least it's not wrong.
                                if(!res) return false;
                                sweep=true;
                            }
                        }
                    }
                }
                else if(var instanceof intnum_bounds)
                {
                    // possibly do bound singleton consistency here
                    System.out.println("Warning: doing no singleton consistency on bounds variable");
                }
                else
                {
                    System.out.println("Warning: doing no singleton consistency on unknown variable");
                }
            }
        }
        
        // if no domains are emptied then return false
        return true;
    }
    
    private boolean sactest(mid_domain var, int val)
    {
        // test var,val to see if it is singleton arc-consistent
        // i.e. if instantiating var=val leads to non-arc-consistent problem
        backtrack.add_backtrack_level();
        
            assert var.is_present(val);
            var.instantiate(val);
            boolean result=propagate();
        
        backtrack.backtrack();
        
        return result;
    }
    
    private int nodenumber=1;
    
    public void savetree(String filename)
    {
        if(root!=null)
        {
            try
            {
            File output = new File(filename);

            FileWriter out = new FileWriter(output);

            out.write(printtree());

            out.close();
            }
            catch(java.io.IOException e)
            {
                System.out.println("Problem writing "+filename);
            }
        }
        else
        {
            System.out.println("error: no tree to save.");
        }
    }
    
    public String printtree()
    {
        // print out the tree in DOT format
        if(root==null) return "";
        
        String st="digraph G {\n";
        
        if(root.var!=null && root.edges!=null)
        {
            for(int i=0; i<root.edges.length; i++)
                st+=printtree(root.edges[i], 0, i);
            
            st+="0 [label=\""+root.var.name+"\"];\n";
        }
        
        st+="}";
        nodenumber=1;
        return st;
    }
    
    private String printtree(tree node, int connectedfrom, int edgelabel)
    {
        String st="";
        if(node!=null)
        {
            if(node.trueleaf)
            {
                st+=connectedfrom+" -> "+nodenumber+" [taillabel="+edgelabel+"];\n";
                st+=nodenumber+" [label=true];\n";
                nodenumber++;
            }
            else if(node.var!=null && node.edges!=null)
            {
                st+=connectedfrom+" -> "+nodenumber+" [taillabel="+edgelabel+"];\n";
                st+=nodenumber+" [label=\""+node.var.name+"\"];\n";
                int newconfrom=nodenumber;
                nodenumber++;
                
                for(int i=0; i<node.edges.length; i++)
                {
                    st+=printtree(node.edges[i], newconfrom, i-((mid_domain)node.var).offset());
                }
            }
        }
        return st;
    }
    
    private heuristic heu=null;
    
    public void setHeuristic(heuristic h1)
    {
        heu=h1;
    }
    
    final class constraint_queue
    {
        qcsp problem;
        // A duplicate-free set of constraints that implement make_ac
        boolean [] make_ac_present;
        ArrayList<make_ac> make_ac_queue;
        
        ArrayList<fd_propagate> fd_propagate_queue;   // queue of constraints, to call the propagate method on.
        TIntArrayList propagate_queue_var;
        TIntArrayList propagate_queue_val;
        
        // low priority queue
        ArrayList<fd_propagate> lp_propagate_queue;
        TIntArrayList lp_propagate_queue_var;
        TIntArrayList lp_propagate_queue_val;
        
        // constraints that need to have establish() called.
        ArrayList<constraint_iface> toestablish;
        
        final boolean printfails=false;  // whether to print the failed constraints
        
        constraint_queue(qcsp problem)
        {
            this.problem=problem;
            //make_ac_queue = new THashSet();  // replace with arraylist and bool array.

            fd_propagate_queue = new ArrayList<fd_propagate>(10000);
            propagate_queue_var = new TIntArrayList(10000);
            propagate_queue_val = new TIntArrayList(10000);

            lp_propagate_queue = new ArrayList<fd_propagate>();
            lp_propagate_queue_var = new TIntArrayList();
            lp_propagate_queue_val = new TIntArrayList();

            toestablish= new ArrayList<constraint_iface>();
        }
        
        void push_constraint(make_ac cons)
        {
            constraint c1=(constraint)cons;
            if(!make_ac_present[c1.id])
            {
                make_ac_present[c1.id]=true;
                make_ac_queue.add(cons);
            }
            //make_ac_queue.add(cons);
            //System.out.println("Adding has_make_ac constraint to the queue");
        }

        void push_constraint(fd_propagate cons, int var, int val)
        {
            if(cons instanceof gac_schema)   // hack hack hack. bug.
            {
                lp_propagate_queue.add(cons);
                lp_propagate_queue_var.add(var);
                lp_propagate_queue_val.add(val);
            }
            else
            {
                fd_propagate_queue.add(cons);
                propagate_queue_var.add(var);
                propagate_queue_val.add(val);
            }
        }
        
        boolean process_all()
        {
            // make_ac constraints and regular fd_propagate constraints get similar treatment
            // then lp constraints are done.

            while(make_ac_queue.size()>0 || fd_propagate_queue.size()>0 || lp_propagate_queue.size()>0)
            {
                while(make_ac_queue.size()>0)
                {
                    //make_ac make_ac_cons=pop_make_ac_cons();
                    //System.out.println(cons.print());
                    make_ac make_ac_cons=make_ac_queue.remove(make_ac_queue.size()-1);
                    constraint c1=(constraint)make_ac_cons;
                    make_ac_present[c1.id]=false;

                    //ICI
                    propagations++;
                    boolean temp = make_ac_cons.make_ac();
                    if(!temp)
                    {
                        if(printfails)
                        { 
                            System.out.println("Failed constraint: "+make_ac_cons+" : ");
                            for(variable_iface v: make_ac_cons.variables())
                            {
                                System.out.print(v.toString()+" ");
                                System.out.println(v.domain());
                            }
                            /*if(make_ac_cons.toString().equals("neg:available[1][4] or neg:fault[1][4] or  <=> neg:shadow[1][4]"))
                            {
                                problem.printdomains();
                                System.exit(-1);
                            }*/
                        }
                        
                        clearqueue();
                        return false;
                    }
                }
                
                while(fd_propagate_queue.size()>0)  // should be if. bug
                {
                    int index=fd_propagate_queue.size()-1;

                    fd_propagate c1=fd_propagate_queue.remove(index);
                    int var=propagate_queue_var.remove(index);
                    int val=propagate_queue_val.remove(index);
                    boolean temp = c1.propagate(var, val);
                    if(!temp)
                    {
                        if(printfails) System.out.println("Failed constraint: "+c1+" var: "+var+" val: "+val);
                        clearqueue();
                        return false;
                    }
                }
                
                while(make_ac_queue.size()==0 && fd_propagate_queue.size()==0 && lp_propagate_queue.size()>0)
                {
                    // do an lp constraint.
                    int index=lp_propagate_queue.size()-1;
                    
                    fd_propagate c1=lp_propagate_queue.remove(index);
                    int var=lp_propagate_queue_var.remove(index);
                    int val=lp_propagate_queue_val.remove(index);
                    boolean temp = c1.propagate(var, val);
                    if(!temp)
                    {
                        if(printfails) System.out.println("Failed constraint: "+c1+" var: "+var+" val: "+val);
                        clearqueue();
                        return false;
                    }
                }
            }
            return true;
        }
        
        boolean establish()
        {
            make_ac_present=new boolean[problem.constraints.size()];
            make_ac_queue=new ArrayList<make_ac>(problem.constraints.size());
            
            for(constraint_iface cons : toestablish)
            {
                if(!cons.establish())
                {
                    if(printfails) System.out.println("Failed constraint:"+cons);
                    return false;
                }
            }
            
            toestablish.clear();
            
            return process_all();
        }
        
        void clearqueue()
        {
            //make_ac_queue.clear();
            
            for(int i=make_ac_queue.size()-1; i>=0; i--)
            {
                constraint c1=(constraint) make_ac_queue.remove(i);
                make_ac_present[c1.id]=false;
            }
            fd_propagate_queue.clear();
            propagate_queue_var.clear();
            propagate_queue_val.clear();
            lp_propagate_queue.clear();
            lp_propagate_queue_var.clear();
            lp_propagate_queue_val.clear();
            toestablish.clear();
        }
    }
}

