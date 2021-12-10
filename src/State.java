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
     * 1. Stack을 상속 받고 Stack은 Vector를 상속받음.
     * 2. Stack Top(1)의 실제 인덱스 = this.size()-1 => 반복문 시작
     * 3. Stack Bottom의 실제 인덱스 = 0 => 반복문 끝
     * @param id
     * @return Stack = size()-i <=> (Vector = i) || no value = -1
     */
    public int lookup (Identifier id) {
        for (Pair pair : this) {
            if(pair.id.equals(id)) return search(pair);
        }
        return -1;
    }

    /**
     * (4) Set Function Implementation
     * 1. stack top index = size()-1.
     * 2. vector 클래스의 set 호출
     * @param id, val
     * @Return State -> this : 자기자신 리턴
     */
    public State set(Identifier id, Value val) {
        super.set(size()-lookup(id), new Pair(id, val));
        return this;
    }

    /**
     * (5) Get Function Implementation
     * 1. State의 lookup 호출 -> vector에서의 index = size()-lookup(id)
     * 2. vector 클래스의 get 호출
     * 3. 없을 경우 null리턴
     * @param id
     * @return value
     */
    public Value get (Identifier id) {
        for (Pair pair : this) {
            if(pair.id.equals(id)) return pair.val;
        }
        return null;
    }



}