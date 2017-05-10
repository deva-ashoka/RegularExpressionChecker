//ToC Bonus Project
//Name: Deva Surya Vivek Madala

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;


//state  for NFA:
class StateNFA {
    int number;
    boolean finalState;
    Map<Character, ArrayList<StateNFA>> nextState;

    public StateNFA(int num) {
        this.number = num;
        this.finalState = false;
        this.nextState = new HashMap<>();
    }

    public void addTransition(StateNFA next, char c) {
        ArrayList<StateNFA> l = this.nextState.get(c);
        if (l == null) {
            l = new ArrayList<>();
            this.nextState.put(c, l);
        }
        l.add(next);
    }

    public ArrayList<StateNFA> getAllTransitionsOfStateForChar(char ch) {
        if (this.nextState.get(ch) == null) {
            return new ArrayList<>();
        } else {
            return this.nextState.get(ch);
        }
    }
}

//NFA machine: a linked list of the nfa states
class NFA {

    LinkedList<StateNFA> listOfStatesNFA;

    public NFA() {
        this.listOfStatesNFA = new LinkedList<>();
    }

}

public class RegularExpressionChecker {

    public static int nfaStateNumber = 0;
    public static Stack<NFA> stackForNFA = new Stack<>();
    public static Stack<Character> stackForOps = new Stack<>();

    //define the operations possible
    public static char unionOperation = 'U';
    public static char concatenationOperation = '.';
    public static char starOperation = '*';

    public static List<Character> validInputChars = new ArrayList<>();

    public static boolean checkForFinalState(ArrayList<StateNFA> branches) {
        for (StateNFA s : branches) {
            if (s.finalState) {
                return true;
            }
        }
        return false;
    }

    public static ArrayList<StateNFA> addEpsilonTransitions(StateNFA state, ArrayList<StateNFA> branches) {

        ArrayList<StateNFA> epsilonTransitionStates = state.getAllTransitionsOfStateForChar('e');

        if (!epsilonTransitionStates.isEmpty()) {
            for (StateNFA eS : epsilonTransitionStates) {
                if (!branches.contains(eS)) {
                    branches.add(eS);
                    branches = addEpsilonTransitions(eS, branches);
                }
            }
        }

        return branches;
    }

    public static void printStateNumberInList(ArrayList<StateNFA> list) {
        for (StateNFA state : list) {
            System.out.print(state.number);
        }
    }

    public static boolean traverseStringOnNFA(NFA nfa, String str) {

        ArrayList<StateNFA> branches = new ArrayList<>();

        //add the first state
        StateNFA firstStateOfNFA = nfa.listOfStatesNFA.getFirst();
        branches.add(firstStateOfNFA);

        char[] string = str.toCharArray();
        int length = str.length();

        for (int i = 0; i < length; i++) {

            ArrayList<StateNFA> temp = new ArrayList<>();
            temp.addAll(branches);

            //add epsilon transitions before processing the input character
            for (StateNFA s : temp) {
                branches = addEpsilonTransitions(s, branches);
            }


            temp.clear();
            temp.addAll(branches);
            //add all the transition states of each branch for that character
            for (StateNFA s : temp) {
                //remove the current states from branches from which transitions are taking place
                branches.remove(s);
                ArrayList<StateNFA> transitionStates = s.getAllTransitionsOfStateForChar(string[i]);
                if (!transitionStates.isEmpty()) {
                    branches.addAll(transitionStates);
                }
            }

            temp.clear();
            temp.addAll(branches);

            for (StateNFA s : temp) {
                branches = addEpsilonTransitions(s, branches);
            }

        }

        return checkForFinalState(branches);
    }

    public static NFA createSubNFA(char input) {

        StateNFA stateNFA1 = new StateNFA(nfaStateNumber);
        nfaStateNumber += 1;
        StateNFA stateNFA2 = new StateNFA(nfaStateNumber);
        nfaStateNumber += 1;
        stateNFA1.addTransition(stateNFA2, input);
        NFA subNfa = new NFA();
        subNfa.listOfStatesNFA.addLast(stateNFA1);
        subNfa.listOfStatesNFA.addLast(stateNFA2);

        return subNfa;

    }


    public static NFA nfaUnion(NFA nfa1, NFA nfa2) {

        //first and last states of NFA1 and NFA2
        StateNFA firstStateOfNFA1 = nfa1.listOfStatesNFA.getFirst();
        StateNFA lastStateOfNFA1 = nfa1.listOfStatesNFA.getLast();
        StateNFA firstStateOfNFA2 = nfa2.listOfStatesNFA.getFirst();
        StateNFA lastStateOfNFA2 = nfa2.listOfStatesNFA.getLast();

        //creating two new states to make Union between NFA1 and NFA2
        StateNFA stateBegin = new StateNFA(nfaStateNumber);
        nfaStateNumber += 1;
        StateNFA stateFinish = new StateNFA(nfaStateNumber++);
        nfaStateNumber += 1;

        //adding epsilon moves between first state of NFA1 and begin state and first state of NFA2 and begin state
        stateBegin.addTransition(firstStateOfNFA1, 'e');
        stateBegin.addTransition(firstStateOfNFA2, 'e');

        //adding epsilon moves between last state of NFA1 and finish state and last state of NFA2 and finish state
        lastStateOfNFA1.addTransition(stateFinish, 'e');
        lastStateOfNFA2.addTransition(stateFinish, 'e');

        //combining both the NFAs into a single nfa
        nfa1.listOfStatesNFA.addFirst(stateBegin);
        nfa2.listOfStatesNFA.addLast(stateFinish);
        for (StateNFA everyState : nfa2.listOfStatesNFA) {
            nfa1.listOfStatesNFA.addLast(everyState);
        }
        return nfa1;
    }

    public static NFA nfaConcatenation(NFA nfa1, NFA nfa2) {

        //adding epsilon move from last state of NFA1 to first state of NFA2
        StateNFA lastStateOfNFA1 = nfa1.listOfStatesNFA.getLast();
        StateNFA firstStateOfNFA2 = nfa2.listOfStatesNFA.getFirst();
        lastStateOfNFA1.addTransition(firstStateOfNFA2, 'e');

        //combining both the NFAs into a single nfa
        for (StateNFA everyState : nfa2.listOfStatesNFA) {
            nfa1.listOfStatesNFA.addLast(everyState);
        }
        return nfa1;
    }

    public static NFA nfaStar(NFA nfa1) {

        StateNFA beginState = new StateNFA(nfaStateNumber);
        nfaStateNumber += 1;
        StateNFA finishState = new StateNFA(nfaStateNumber);
        nfaStateNumber += 1;

        StateNFA firstStateOfNFA1 = nfa1.listOfStatesNFA.getFirst();
        StateNFA lastStateOfNFA1 = nfa1.listOfStatesNFA.getLast();

        //adding epsilon moves
        beginState.addTransition(firstStateOfNFA1, 'e');
        beginState.addTransition(finishState, 'e');
        lastStateOfNFA1.addTransition(firstStateOfNFA1, 'e');
        lastStateOfNFA1.addTransition(finishState, 'e');

        //add the begin and final states to the nfa
        nfa1.listOfStatesNFA.addFirst(beginState);
        nfa1.listOfStatesNFA.addLast(finishState);

        return nfa1;
    }

    public static boolean checkPriority(char operation1, char operation2) {

        if (operation1 == operation2) {
            return true;
        } else if (operation1 == starOperation) {
            return false;
        } else if (operation2 == starOperation) {
            return true;
        } else if (operation1 == concatenationOperation) {
            return false;
        } else if (operation2 == concatenationOperation) {
            return true;
        } else if (operation1 == unionOperation) {
            return false;
        } else {
            return true;
        }
    }

    public static void nfaOperation() {
        if (stackForOps.size() > 0) {
            char operationToBePerformed = stackForOps.pop();

            if (operationToBePerformed == unionOperation) {
                NFA nfa2 = stackForNFA.pop();
                NFA nfa1 = stackForNFA.pop();
                NFA nfaU = nfaUnion(nfa1, nfa2);
                stackForNFA.push(nfaU);
            } else if (operationToBePerformed == concatenationOperation) {
                NFA nfa2 = stackForNFA.pop();
                NFA nfa1 = stackForNFA.pop();
                NFA nfaC = nfaConcatenation(nfa1, nfa2);
                stackForNFA.push(nfaC);
            } else if (operationToBePerformed == starOperation) {
                NFA nfa1 = stackForNFA.pop();
                NFA nfaS = nfaStar(nfa1);
                stackForNFA.push(nfaS);
            }
        }
    }

    public static NFA generateNFAFromRegex(String regexString) {


        NFA finalNFA;
        int length = regexString.length();
        char[] regex = regexString.toCharArray();

        for (int i = 0; i < length; i++) {

            //create a sub nfa for 1 or 0
            if (validInputChars.contains(regex[i])) {
                NFA subNFA = createSubNFA(regex[i]);
                stackForNFA.push(subNFA);

            } else if (stackForOps.isEmpty()) {
                stackForOps.push(regex[i]);

            } else if (regex[i] == '(') {
                stackForOps.push(regex[i]);

                //if the char is ')', then do the necessary operations inside the parentheses

            } else if (regex[i] == ')') {

                while (stackForOps.get(stackForOps.size() - 1) != '(') {
                    nfaOperation();
                }
                //after doing all the operations, the only char left will be '('
                if (stackForOps.get(stackForOps.size() - 1) == '(') {
                    stackForOps.pop();
                }

            } else {
                while (!stackForOps.isEmpty() && checkPriority(regex[i], stackForOps.get(stackForOps.size() - 1))) {
                    nfaOperation();
                }
                stackForOps.push(regex[i]);
            }
        }
        while (!stackForOps.isEmpty()) {
            nfaOperation();
        }

        finalNFA = stackForNFA.pop();

        //make the last state of final as final state
        StateNFA lastStateOfNFA = finalNFA.listOfStatesNFA.getLast();
        lastStateOfNFA.finalState = true;

        return finalNFA;
    }


    public static String addConcatenationSymbolToRegex(String regex) {
        String retVal = "";
        char[] regexTokens = regex.toCharArray();
        int length = regex.length();

        for (int i = 0; i < length - 1; i++) {

            if (validInputChars.contains(regexTokens[i]) && validInputChars.contains(regexTokens[i + 1])) {
                retVal += regexTokens[i] + ".";
            } else if (validInputChars.contains(regexTokens[i]) && regexTokens[i + 1] == '(') {
                retVal += regexTokens[i] + ".";
            } else if (regexTokens[i] == ')' && validInputChars.contains(regexTokens[i + 1])) {
                retVal += regexTokens[i] + ".";
            } else if (regexTokens[i] == '*' && validInputChars.contains(regexTokens[i + 1])) {
                retVal += regexTokens[i] + ".";
            } else if (regexTokens[i] == '*' && regexTokens[i + 1] == '(') {
                retVal += regexTokens[i] + ".";
            } else if (regexTokens[i] == ')' && regexTokens[i + 1] == '(') {
                retVal += regexTokens[i] + ".";
            } else {
                retVal += regexTokens[i];
            }
        }
        retVal += regexTokens[length - 1];
        return retVal;
    }

    public static String removeWhiteSpaces(String str) {
        String retVal = "";
        char[] strTokens = str.toCharArray();
        int length = str.length();
        for (int i = 0; i < length; i++) {
            if (strTokens[i] != ' ') {
                retVal += strTokens[i];
            }
        }
        return retVal;
    }


    public static void main(String[] args) {
        try {
            String fileName = args[0];
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String regex = br.readLine();
            String input = br.readLine();

            validInputChars.add('1');
            validInputChars.add('0');

            regex = removeWhiteSpaces(regex);
            input = removeWhiteSpaces(input);

            regex = addConcatenationSymbolToRegex(regex);

            NFA nfa = generateNFAFromRegex(regex);

            int numberOfStates = nfa.listOfStatesNFA.size();

            System.out.println("Regular Expression: " + regex);

            System.out.println("===================== NFA CREATED =====================");


            for (int i = 0; i < numberOfStates; i++) {
                StateNFA state = nfa.listOfStatesNFA.get(i);
                System.out.println("------ State Number: " + state.number + " --------");
                System.out.print("Transitions for 1: ");
                ArrayList<StateNFA> states1 = state.getAllTransitionsOfStateForChar('1');
                if (!states1.isEmpty()) {
                    for (StateNFA s : states1) {
                        System.out.print("State " + s.number + ", ");
                    }
                }
                System.out.println();
                System.out.print("Transitions for 0: ");
                ArrayList<StateNFA> states0 = state.getAllTransitionsOfStateForChar('0');
                if (!states0.isEmpty()) {
                    for (StateNFA s : states0) {
                        System.out.print("State " + s.number + ", ");
                    }
                }
                System.out.println();
                System.out.print("Transitions for e: ");
                ArrayList<StateNFA> statese = state.getAllTransitionsOfStateForChar('e');
                if (!statese.isEmpty()) {
                    for (StateNFA s : statese) {
                        System.out.print("State " + s.number + ", ");
                    }
                }
                System.out.println();
            }

            System.out.println("======================================================");


            System.out.println("Input string: " + input);
            boolean inputValid = traverseStringOnNFA(nfa, input);
            if (inputValid) {
                System.out.println("True");
            } else {
                System.out.println("False");
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
