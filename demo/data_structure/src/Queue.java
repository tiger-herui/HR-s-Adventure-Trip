import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Queue {
    Deque<Integer> stack1;
    Deque<Integer> stack2;

    public Queue() {
        stack1 = new LinkedList<Integer>();
        stack2 = new LinkedList<Integer>();
    }

    public void appendTail(int value) {
        stack1.push(value);
    }

    public int deleteHead() {
        if (stack2.isEmpty()) {
            while (!stack1.isEmpty()) {
                stack2.push(stack1.pop());
            }
        }
        if (stack2.isEmpty()) {
            return -1;
        } else {
            int deleteItem = stack2.pop();
            return deleteItem;
        }
    }

    public int deleteHead2() {
        if(!stack2.isEmpty()) return stack2.removeLast();
        if(stack1.isEmpty()) return -1;
        while(!stack1.isEmpty())
            stack2.addLast(stack1.removeLast());
        return stack2.removeLast();
    }

}
