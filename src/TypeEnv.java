// Type environment for S
import java.util.*;

// <id, type> 
class Entry {
   Identifier id;
   Type type;
   
   Entry (Identifier id, Type t) {
     this.id = id;
     this.type = t;
   }
}

class TypeEnv extends Stack<Entry> {
    public TypeEnv() { }
    
    public TypeEnv(Identifier id, Type t) {
        push(id, t);
    }
    
    public TypeEnv push(Identifier id, Type t) {
        super.push(new Entry(id, t));
	    return this;
    }
    
    /**
     * (1) Contatins Function Implementation
     * super class(vector)의 contains 사용.
     * @param id
     * @return boolean
     */
    public boolean contains (Identifier id) {
        for (Entry entry : this) {
            if(entry.id.equals(id)) return true;
        }
        return false;
    }

    /**
     * (2) Get Function Implementation
     * 1. vector의 마지막 원소(Stack top)에서 부터 loop을 돌면서 id에 맞는 값 확인
     * 2. 해당 인덱스의 type 리턴
     * 3. id 값이 없으면 null 리턴
     * @param id
     * @return Type
     */
    public Type get (Identifier id) {
        for (Entry entry : this) {
            if(entry.id.equals(id)) return entry.type;
        }
        return null;
    }
}