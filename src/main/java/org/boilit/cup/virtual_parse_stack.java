package org.boilit.cup;

/**
 * This class implements a temporary or "virtual" parse stack that
 * replaces the top portion of the actual parse stack (the part that
 * has been changed by some set of operations) while maintaining its
 * original contents.  This data expression is used when the parse needs
 * to "parse ahead" to determine if a given error recovery attempt will
 * allow the parse to continue far enough to consider it successful.  Once
 * success or failure of parse ahead is determined the system then
 * reverts to the original parse stack (which has not actually been
 * modified).  Since parse ahead does not execute actions, only parse
 * state is maintained on the virtual stack, not full Symbol objects.
 *
 * @author Frank Flannery
 * @version last updated: 7/3/96
 * @see org.boilit.cup.lr_parser
 */
public final class virtual_parse_stack {
    /*-----------------------------------------------------------*/
    /*--- Constructor(s) ----------------------------------------*/
    /*-----------------------------------------------------------*/

    /**
     * Constructor to build a virtual stack out of a real stack.
     */
    public virtual_parse_stack(Stack shadowing_stack) throws Exception {
        /* sanity check */
        if (shadowing_stack == null)
            throw new Exception("Internal parser error: attempt to create null virtual stack");

        /* set up our internals */
        real_stack = shadowing_stack;
        vstack = new ArrayStack();
        real_next = 0;

        /* get one element onto the virtual portion of the stack */
        get_from_real();
    }

    /*-----------------------------------------------------------*/
    /*--- (Access to) Instance Variables ------------------------*/
    /*-----------------------------------------------------------*/

    /**
     * The real stack that we shadow.  This is accessed when we move off
     * the bottom of the virtual portion of the stack, but is always line
     * unmodified.
     */
    protected Stack<Symbol> real_stack;

    /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

    /**
     * Top of stack indicator for where we leave off in the real stack.
     * This is measured from top of stack, so 0 would indicate that no
     * elements have been "moved" from the real to virtual stack.
     */
    protected int real_next;

    /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

    /**
     * The virtual top portion of the stack.  This stack contains Integer
     * objects with state numbers.  This stack shadows the top portion
     * of the real stack within the area that has been modified (via operations
     * on the virtual stack).  When this portion of the stack becomes empty we
     * transfer elements from the underlying stack onto this stack.
     */
    protected Stack vstack;

    /*-----------------------------------------------------------*/
    /*--- General Methods ---------------------------------------*/
    /*-----------------------------------------------------------*/

    /**
     * Transfer an element from the real to the virtual stack.  This assumes
     * that the virtual stack is currently empty.
     */
    protected void get_from_real() {
        /* don't transfer if the real stack is empty */
        if (real_next >= real_stack.size()) return;

        /* get a copy of the first Symbol we have not transfered */
        Symbol stack_sym = real_stack.peek(real_next);

        /* record the transfer */
        real_next++;

        /* put the state number from the Symbol onto the virtual stack */
        vstack.push(stack_sym.parse_state);
    }

    /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

    /**
     * Indicate whether the stack is empty.
     */
    public boolean empty() {
        /* if vstack is empty then we were unable to transfer onto it and the whole thing is empty. */
        return vstack.empty();
    }

    /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

    /**
     * Return value on the top of the stack (without popping it).
     */
    public int top() throws Exception {
        if (vstack.empty())
            throw new Exception(
                    "Internal parser error: top() called on empty virtual stack");

        return ((Integer) vstack.peek()).intValue();
    }

    /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

    /**
     * Pop the stack.
     */
    public void pop() throws Exception {
        if (vstack.empty())
            throw new Exception("Internal parser error: pop from empty virtual stack");

        /* pop it */
        vstack.pop();

        /* if we are now empty transfer an element (if there is one) */
        if (vstack.empty())
            get_from_real();
    }

    /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

    /**
     * Push a state number onto the stack.
     */
    public void push(int state_num) {
        vstack.push(state_num);
    }

    /*-----------------------------------------------------------*/

}