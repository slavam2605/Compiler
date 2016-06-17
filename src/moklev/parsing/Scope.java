package moklev.parsing;

import javafx.util.Pair;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * @author Моклев Вячеслав
 */
public class Scope {

    private LinkedList<HashMap<String, Pair<Integer, TypeInstance>>> stack;

    public Scope() {
        stack = new LinkedList<>();
    }

    public void put(String s, Integer offset, TypeInstance typeInstance) {
        if (stack.isEmpty()) {
            throw new IllegalStateException("No current scope");
        } else {
            stack.peekLast().put(s, new Pair<>(offset, typeInstance));
        }
    }

    @SuppressWarnings("unchecked")
    public void enterScope() {
        stack.addLast(new HashMap<>());
    }

    public int leaveScope() {
        if (!stack.isEmpty()) {
            int result = stack.peekLast().size();
            stack.pollLast();
            return result;
        } else {
            throw new IllegalStateException("Scope is already empty");
        }
    }

    public int countLocalScopes() {
        int result = 0;
        for (HashMap<?, ?> map: stack) {
            result += map.size();
        }
        return result - stack.peekFirst().size();
    }

    public int get(String s) {
        ListIterator<HashMap<String, Pair<Integer, TypeInstance>>> iterator = stack.listIterator(stack.size());
        while (iterator.hasPrevious()) {
            HashMap<String, Pair<Integer, TypeInstance>> currentScope = iterator.previous();
            Pair<Integer, TypeInstance> entry = currentScope.get(s);
            if (entry != null) {
                return entry.getKey();
            }
        }
        throw new IllegalArgumentException("No such element in scope: " + s);
    }

    public TypeInstance getType(String s) {
        ListIterator<HashMap<String, Pair<Integer, TypeInstance>>> iterator = stack.listIterator(stack.size());
        while (iterator.hasPrevious()) {
            HashMap<String, Pair<Integer, TypeInstance>> currentScope = iterator.previous();
            Pair<Integer, TypeInstance> entry = currentScope.get(s);
            if (entry != null) {
                return entry.getValue();
            }
        }
        throw new IllegalArgumentException("No such element in scope: " + s);
    }

}
