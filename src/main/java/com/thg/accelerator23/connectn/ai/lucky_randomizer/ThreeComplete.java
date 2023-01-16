package com.thg.accelerator23.connectn.ai.lucky_randomizer;

import com.thehutgroup.accelerator.connectn.player.*;
//import com.thg.accelerator23.connectn.ai.lucky_randomizer.LuckyRandomX;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ThreeComplete extends Player {
    private GameConfig config;

    private Function<Position, Position> hMover = p -> new Position(p.getX() + 1, p.getY());
    private Function<Position, Position> vMover = p -> new Position(p.getX(), p.getY() + 1);
    private Function<Position, Position> diagUpRightMover = hMover.compose(vMover);
    private Function<Position, Position> diagUpLeftMover =
            p -> new Position(p.getX() - 1, p.getY() + 1);
    private Map<Function<Position, Position>, List<Position>> positionsByFunction;

    public Map<Function<Position, Position>, List<Position>> positionsAndMovers() {
        positionsByFunction = new HashMap<>();
        List<Position> leftEdge = IntStream.range(0, 8)
                .mapToObj(Integer::new)
                .map(i -> new Position(0, i))
                .collect(Collectors.toList());
        List<Position> bottomEdge = IntStream.range(0, 10)
                .mapToObj(Integer::new)
                .map(i -> new Position(i, 0))
                .collect(Collectors.toList());
        List<Position> rightEdge = leftEdge.stream()
                .map(p -> new Position(9, p.getY()))
                .collect(Collectors.toList());

        List<Position> leftBottom = Stream.concat(leftEdge.stream(),
                bottomEdge.stream()).distinct().collect(Collectors.toList());
        List<Position> rightBottom = Stream.concat(rightEdge.stream(),
                bottomEdge.stream()).distinct().collect(Collectors.toList());

        positionsByFunction.put(hMover, leftEdge);
        positionsByFunction.put(vMover, bottomEdge);
        positionsByFunction.put(diagUpRightMover, leftBottom);
        positionsByFunction.put(diagUpLeftMover, rightBottom);

        return positionsByFunction;
    }

    public int makeMove(Board board) {
        List<Line> lines = getLines(board);
        List<Position> allStartPositions = getAllStartPositions();
        List<Function<Position, Position>> allMovementFunctions = getAllMovementFunctions();
        Map<Counter, Integer[]> bestRunByColour = new HashMap<>();
        Integer[] RunParametersO = new Integer[3];
        RunParametersO[0] = 0;
        RunParametersO[1] = 15;
        RunParametersO[2] = 15;
        Integer[] RunParametersX = new Integer[3];
        RunParametersX = RunParametersO;
        Integer[] useThisRun = new Integer[3];
        Position useThisStartPosition = new Position(10, 10);
        Function<Position, Position> useThisMovementFunction = hMover;
        Position candidate1 = new Position(20, 20);
        Position candidate2 = new Position(20, 20);
        Function<Position, Position> bestMoveO = hMover;
        Function<Position, Position> bestMoveX = hMover;
        Position startO = new Position(0, 0);
        Position startX = new Position(0, 0);
        for (int i=0; i<lines.size(); i++) {
            Map<Counter, Integer[]> bestRunInLine = GetBestRunByColour(lines.get(i));
            bestRunByColour = MaxMap(bestRunInLine, bestRunByColour);
            Integer[] runParametersO = bestRunByColour.get(Counter.O);
            Integer[] runParametersX = bestRunByColour.get(Counter.X);
            bestRunByColour.entrySet();
            if (runParametersO[0]>RunParametersO[0]) {
                RunParametersO = runParametersO;
                bestMoveO = allMovementFunctions.get(i);
                startO = allStartPositions.get(i);
            }
            if (runParametersX[0]>RunParametersX[0]) {
                RunParametersX = runParametersX;
                bestMoveX = allMovementFunctions.get(i);
                startX = allStartPositions.get(i);
            }
        }
        if (RunParametersO[0]>=RunParametersX[0]) {
            useThisRun = RunParametersO;
            useThisStartPosition = startO;
            useThisMovementFunction = bestMoveO;
        } else {
            useThisRun = RunParametersX;
            useThisStartPosition = startX;
            useThisMovementFunction = bestMoveX;
        }
        if (useThisRun[1]>0) {
            for (int k=0; k<useThisRun[1]; k++) {
                candidate1 = useThisMovementFunction.apply(useThisStartPosition);
            }
        } else {
            candidate1 = useThisStartPosition;
        }
        for (int w=0; w<useThisRun[2]; w++) {
            candidate2 = useThisMovementFunction.apply(useThisStartPosition);
        }
        int possibleMove1 = getMinVacantY(board, candidate1);
        int possibleMove2 = getMinVacantY(board, candidate2);
        if (possibleMove1==candidate1.getY()) {
            return candidate1.getX();
        } else if (possibleMove2==candidate2.getY()) {
            return candidate2.getY();
        } else {
            CheckWhichColumnsAreEmpty checkWhichColumnsAreEmpty = new CheckWhichColumnsAreEmpty(board);
            List<Integer> emptyColumns = checkWhichColumnsAreEmpty.fullColumnChecker();
            int randomNumber = new Random().nextInt(0, emptyColumns.size());
            return emptyColumns.get(randomNumber);
        }
    }

    private int getMinVacantY(Board board, Position p) {
        int x = p.getX();
        for(int i = 7; i >= 0; --i) {
            Position pTemp = new Position(x, i);
            if (i == 0 || board.hasCounterAtPosition(pTemp) == true) {
                return i;
            }
        }

        throw new RuntimeException("no y is vacant");
    }

//    BoardAnalyser boardChecker = new BoardAnalyser(config);

public ThreeComplete(Counter counter) {
    //TODO: fill in your name here
    super(counter, ThreeComplete.class.getName());
}

    public boolean isBoardFull(Board board) {
        return IntStream.range(0, board.getConfig().getWidth())
                .allMatch(
                        i -> board.hasCounterAtPosition(new Position(i, board.getConfig().getHeight() - 1)));
    }

    private Map<Counter, Integer[]> GetBestRunByColour(Line line) {
        HashMap<Counter, Integer[]> bestRunByColour = new HashMap<>();
        Integer[] numbers = new Integer[3];
        numbers[0] = 0;
        numbers[1] = 0;
        numbers[2] = 0;
        int location = 0;
        for (Counter c : Counter.values()) {
            bestRunByColour.put(c, numbers);
        }
        Counter current = null;
        int currentRunLength = 0;
        while (line.hasNext()) {
            Counter next = line.next();
            if (current != next) {
                if (current != null) {
                    numbers[2] = location;
                    if (Math.max(currentRunLength, 1) > bestRunByColour.get(current)[0]) {
                        numbers[0] = Math.max(currentRunLength, 1);
                        bestRunByColour.put(current, numbers);
                    }
                }
                if (next != null) {
                    numbers[1] = location;
                }
                currentRunLength = 1;
                current = next;
            } else {
                currentRunLength++;
            }
            location++;
        }
        if (current != null && Math.max(currentRunLength, 1) > bestRunByColour.get(current)[0]) {
            bestRunByColour.put(current, numbers);
        }
        return bestRunByColour;
    }

    private Map<Counter, Integer[]> MaxMap(Map<Counter, Integer[]> map1, Map<Counter, Integer[]> map2) {
        HashMap<Counter, Integer[]> MaxMap = new HashMap<>();
        Integer[] ifNotFound = new Integer[3];
        ifNotFound[0] = 0;
        ifNotFound[1] = 10;
        ifNotFound[2] = 13;
        for (Map.Entry<Counter, Integer[]> entry : map1.entrySet()) {
            Integer[] tempIntArray = map2.getOrDefault(entry.getKey(), ifNotFound);
            if (entry.getValue()[0] > tempIntArray[0]) {
                MaxMap.put(entry.getKey(), entry.getValue());
            } else {
                MaxMap.put(entry.getKey(), tempIntArray);
            }
        }
        return MaxMap;
    }

    private List<Line> getLines(Board board) {
        Map<Function<Position, Position>, List<Position>> positionsByFunction = positionsAndMovers();
        ArrayList<Line> lines = new ArrayList<>();
        List<Function<Position, Position>> allMovementFunctions = new ArrayList<>();
        List<Position> allStartPositions = new ArrayList<>();
        for (Map.Entry<Function<Position, Position>, List<Position>> entry : positionsByFunction
                .entrySet()) {
            Function<Position, Position> function = entry.getKey();
            List<Position> startPositions = entry.getValue();
//      Position pos = startPositions.get(0);
//      int xValue = pos.getX();
            lines.addAll(startPositions.stream().map(p -> new BoardLine(board, p, function))
                    .collect(Collectors.toList()));
            allStartPositions.addAll(startPositions);
            for (int i=0; i<allStartPositions.size(); i++) {
                allMovementFunctions.add(function);
            }
        }
        return lines;
    }

    private List<Position> getAllStartPositions() {
        Map<Function<Position, Position>, List<Position>> positionsByFunction = positionsAndMovers();
        List<Position> allStartPositions = new ArrayList<>();
        for (Map.Entry<Function<Position, Position>, List<Position>> entry : positionsByFunction
                .entrySet()) {
            Function<Position, Position> function = entry.getKey();
            List<Position> startPositions = entry.getValue();
//      Position pos = startPositions.get(0);
//      int xValue = pos.getX();
            allStartPositions.addAll(startPositions);
        }
        return allStartPositions;
    }

    private List<Function<Position, Position>> getAllMovementFunctions() {
        Map<Function<Position, Position>, List<Position>> positionsByFunction = positionsAndMovers();
        List<Function<Position, Position>> allMovementFunctions = new ArrayList<>();
        for (Map.Entry<Function<Position, Position>, List<Position>> entry : positionsByFunction
                .entrySet()) {
            Function<Position, Position> function = entry.getKey();
            List<Position> startPositions = entry.getValue();
//      Position pos = startPositions.get(0);
//      int xValue = pos.getX();
            for (int i=0; i<startPositions.size(); i++) {
                allMovementFunctions.add(function);
            }
        }
        return allMovementFunctions;
    }

//    public int makeMove(Board board) {
//        GameState gameState = boardChecker.calculateGameState(board);
//
//    }

}