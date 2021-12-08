package lexicalAnlyzer;

import java.util.*;
// State as stack 

// <id, val> 
class Pair {
   Identifier id;
   Value val;

   Pair (Identifier id, Value v) {
     this.id = id;
     this.val = v;
   }
}

class State extends Stack<Pair> {
    public State( ) { }

    public State(Identifier id, Value val) {
        push(id, val);
    }

    // (1) Push function Implementation
    public State push(Identifier id, Value val) {
        super.push(new Pair(id, val));
        return this;
    }

    // (2) Pop function Implementation (Optional)
    public Pair pop() {
        return super.pop();
    }

    // (3) Lookup function Implementation
    public int lookup (Identifier v) {
        for (int i = lastIndexOf(lastElement()); i < 0; i--) {
            if (get(i).id == v) {
                return i;
            }
        }
        return -1;
    }

    // (4) Set Function Implementation
    public State set(Identifier id, Value val) {
    	// Set Implementation
    }

    // (5) Get Function Implementation
    public Value get (Identifier id) {
    	// Get Implementation
    }


}