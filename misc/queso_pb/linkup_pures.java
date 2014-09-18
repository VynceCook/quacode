///////////////////////////////////////////////////////////////////////////////////////
// Pure value rule infrastructure
//

package queso.constraints;

import queso.core.*;

public class linkup_pures extends constraint implements fd_propagate
{
    // this is not quite a constraint.
    // It links the extra variables introduced for detecting pure values back
    // to the original problem variable.
    
    mid_domain original;
    int [] watchvariables;  // a watch for each value, index of the variable in extravars.
    mid_domain [] extravars;
    
    mid_domain [] variables;
    
    int lowerbound;
    
    public linkup_pures(mid_domain[] variables, qcsp problem)
    {
        super(problem);
        
        this.variables=variables;
        check_variables(variables);
        original=variables[0];
        
        extravars=new mid_domain[variables.length-1];
        for(int i=1; i<variables.length; i++)
        {
            extravars[i-1]=variables[i];
            assert variables[i] instanceof puremonitor;
            assert ((puremonitor)variables[i]).domsize()==original.domsize();
        }
        
        // add to the appropriate wakeup lists
        for(mid_domain temp : variables)
        {
            temp.add_wakeup(this);
        }
        
        watchvariables= new int[original.domsize()];
        lowerbound=original.lowerbound();
    }
    
    public variable_iface [] variables()
    {
        return (variable_iface []) variables;
    }
    
    public boolean establish()
    {
        // propagate from original to the puremonitors
        for(int i=original.lowerbound(); i<=original.upperbound(); i++)
        {
            if(!original.is_present(i))
            {
                for(mid_domain extra : extravars)
                {
                    if(extra.is_present(i))
                    {
                        extra.exclude(i, null); // don't care if we have domain wipeout of a puremonitor variable.
                    }
                }
            }
        }
        
        // propagate from puremonitors to the original variable
        for(int val=original.lowerbound(); val<=original.upperbound(); val++)
        {
            // find a support for value val.
            boolean found=false;
            for(int var=0; var<extravars.length; var++)
            {
                if(extravars[var].is_present(val))  // not pruned
                {
                    watchvariables[val-lowerbound]=var;
                    //System.out.println("watchvariables[val:"+val+"]=var:"+var);
                    found=true;
                    break;
                }
            }
            
            if(!found)
            {
                // this value is pure
                if(original.is_present(val))
                {
                    System.out.println("Pure value in linkup_pures establish. Variable "+original+" value "+val);
                    original.pure(val, null);   // re-queue this constraint.
                    //if(original instanceof existential) // this bit assumes that the value will have been instantiated.
                    //    return true;
                }
            }
        }
        
        return true;  // never unsatisfied.
    }
    
    public boolean propagate(int problem_var, int val)
    {
        // problem_var is an index into the problem variable array.
        
        int var=variables[0].id();
        
        for(int i=0; i<variables.length; i++)
        {
            if (problem_var==variables[i].id())
            {
                var=i;
                break;
            }
        }
        
        //System.out.println("Propagate called "+var+" = "+val);
        // var, val is the pair removed.
        
        if(var==0)
        {
            // if it is the original
            for(mid_domain extra : extravars)
            {
                // prune the appropriate value of the pure monitor variables
                if(extra.is_present(val))
                {
                    extra.exclude(val, this);
                }
            }
            return true;  // succeed
        }
        
        // The variable that has changed is a puremonitor.
        
        /*if(original.unit())  // optimize the unit case. Speed up only about 1% on connect3-4x4.
        {
            return true;
        }*/
        
        int index=var-1;  // index in the extravars array.
        
        if(watchvariables[val-lowerbound]==index)  // if the "support" for some value has been pruned:
        {
            for(int i=index+1; i<extravars.length; i++)
            {
                if(extravars[i].is_present(val))
                {
                    // we have found a new support
                    watchvariables[val-lowerbound]=i;
                    //System.out.println("watchvariables["+val+"]="+var);
                    return true;
                }
            }
            
            // start again from beginning
            for(int i=0; i<index; i++)
            {
                if(extravars[i].is_present(val))
                {
                    // we have found a new support
                    watchvariables[val-lowerbound]=i;
                    //System.out.println("watchvariables["+val+"]="+var);
                    return true;
                }
            }
            
            // no new support found for val, so it is pure.
            
            if(original.is_present(val)) original.pure(val, null);  // may need to re-queue this constraint.
        }
        
        // warning: it is possible to have all pure values of a universal: what happens then?
        // Do we need to succeed unconditionally at that point? NO - the last pure value is not removed.
        
        return true;
    }
    
    public boolean entailed()
    {   // none of this rubbish
        assert false;
        return false;
    }
    
    public String toString()
    {
        return "linkup_pures "+original+java.util.Arrays.asList(extravars);
    }
}

