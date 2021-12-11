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
     * (1) Contains function Implementation
     * 1. Stack Top(index=1)에서 부터 반복문을 돌림
     *    - 하지만 stack 클래스는 vector를 상속받았기 때문에
     *      실질적으로 저장된 원소의 위치는 '(배열 크기) - (스택탑에서부터의 index)'로
     *      찾아야함.
     *    - 이때, State 클래스에서 get을 overriding 했기 때문에 super.get으로
     *      실제 배열의 위치를 찾아야함.
     * 2. id값 비교후 값이 있다면 true 리턴
     * 3. 없다면 false 리턴
     * @param id
     * @return boolean
     */
    public boolean contains (Identifier id) {
        for (int i = 1; i < size()+1; i++) { // i = 1부터 stack의 크기만큼 반복
            if(super.get(size()-i).id.equals(id)) // 파라미터로 들어온 id과 비교
                return true; // 같다면 true 리턴
        }
        return false; // 없다면 false 리턴
    }

    /**
     * (2) Get Function Implementation
     * 1. lookup method와 같은 방식으로 작동
     * 2. 비교할 Entry를 super.get을 사용해 가져옴
     * 3. 파라미터로 넘어온 id가 있는 Entry의 type를 리턴
     * 4. 없다면 null 리턴
     * @param id
     * @return Type [ Entry의 type || null]
     */
    public Type get (Identifier id) {
        for (int i = 1; i < size()+1; i++) {
            Entry compEntry = super.get(size() - i);
            if(compEntry.id.equals(id)) return compEntry.type;
        }
        return null;
    }
}