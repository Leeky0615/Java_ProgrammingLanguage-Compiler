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

    /**
     * (1) Push function Implementation
     * 1. 파라미터를 새로운 new Pair로 만들고 넘김
     * 2. super class(Stack)의 push사용
     * @param id, val
     * @return this
     */
    public State push(Identifier id, Value val) {
        super.push(new Pair(id, val));
        return this;
    }

    /**
     * (2) Pop function Implementation (Optional)
     * 1. super class(Stack)의 pop 사용.
     * 2. stack top에 있는 원소를 뽑은 후 해당 원소 리턴
     * @return Pair
     */
    public Pair pop() {
        return super.pop();
    }

    /**
     * (3) Lookup function Implementation
     * 1. Stack Top(index=1)에서 부터 반복문을 돌림
     *    - 하지만 stack 클래스는 vector를 상속받았기 때문에
     *      실질적으로 저장된 원소의 위치는 '(배열 크기) - (스택탑에서부터의 index)'로
     *      찾아야함.
     *    - 이때, State 클래스에서 get을 overriding 했기 때문에 super.get으로
     *      실제 배열의 위치를 찾아야함.
     * 2. id값 비교후 값이 있다면 해당 인덱스(stack index) 리턴
     * 3. 없다면 -1 리턴
     * @param id
     * @return int [i || -1]
     */
    public int lookup (Identifier id) {
        for (int i = 1; i < size()+1; i++) { // i = 1부터 stack의 크기만큼 반복
            if(super.get(size()-i).id.equals(id)) // 파라미터로 들어온 id과 비교
                return i; // 같다면 해당 인덱스 리턴
        }
        return -1; // 파라미터(id)값이 없다면 -1 리턴
    }

    /**
     * (4) Set Function Implementation
     * 1. State 클래스의 lookup 사용 stack에서의 index를 받아옴
     * 2. super.set 함수를 사용(overriding)
     *    - 파라미터로 실제 배열의 인덱스를 넣어야 하기 때문에
     *      (배열의 크기) - (stack index)를 해 원래 위치를 구함
     *    - 설정 하려논 Pair를 생성(파라미터로 들어온 id,val 사용)
     * 3. 자기 자신(Stack) 리턴
     * @param id, val
     * @Return State -> this : 자기자신 리턴
     */
    public State set(Identifier id, Value val) {
        super.set(size()-lookup(id), new Pair(id, val)); // super 클래스의 set method 사용
        return this; // 자기자신 리턴
    }

    /**
     * (5) Get Function Implementation
     * 1. lookup method와 같은 방식으로 작동
     * 2. 비교할 Pair를 super.get을 사용해 가져옴
     * 3. 파라미터로 넘어온 id가 있는 Pair의 Value를 리턴
     * 4. 없다면 null 리턴
     * @param id
     * @return Value [ Pair의 value || null]
     */
    public Value get (Identifier id) {
        for (int i = 1; i < size()+1; i++) { // i = 1부터 stack의 크기만큼 반복
            Pair compPair = super.get(size() - i); // 비교할 Pair 가져옴(실제 Array의 위치)
            if(compPair.id.equals(id)) return compPair.val; // 비교 & 리턴
        }
        return null; // 없다면 null 리턴
    }



}