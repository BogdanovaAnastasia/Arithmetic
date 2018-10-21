package org.itstep;

import java.util.Deque;

public class SyntaxParser {
    public static int parseExpression(Deque<Token> tokens) throws UnexpectedTokenException, UnexpectedEndOfExpressionException {
        //Any expression is A, where A is a number of summation and subtraction
        int res = parseAddition(tokens);
        if (!tokens.isEmpty())
            throw new UnexpectedEndOfExpressionException(tokens.peek());
        return res;
    }

    //To find first element in summation/subtraction
    private static int parseAddition(Deque<Token> tokens)  throws UnexpectedTokenException {
        int m = parseMultiplication(tokens);
        return parseAdditionRec(m,tokens);
    }

    //recursively find all summation/subtraction
    private static int parseAdditionRec(int prev, Deque<Token> tokens) throws UnexpectedTokenException {
        //A is M or M+A or M-A, where M is multiplication
        if (tokens.isEmpty())
            return prev;

        Token tok = tokens.peek();
        switch(tok.tokType) {
            case PLS: {
                tokens.poll();
                int m = parseMultiplication(tokens);
                return parseAdditionRec(prev+m,tokens);
            }

            case MNS: {
                tokens.poll();
                int m = parseMultiplication(tokens);
                return parseAdditionRec(prev-m,tokens);
            }

            default:
                return prev;
        }
    }

    private static int parseMultiplication(Deque<Token> tokens) throws UnexpectedTokenException {
        int t = parseFunction(tokens);
        return parseMultiplicationRec(t,tokens);
    }

    private static int parseMultiplicationRec(int prev, Deque<Token> tokens) throws UnexpectedTokenException {
        //M is F*M or F/M or F, where F is function
        if (tokens.isEmpty())
            return prev;

        Token nextTok = tokens.peek();
        switch(nextTok.tokType) {
            case MUL: {
                tokens.poll();
                int t = parseFunction(tokens);
                return parseMultiplicationRec(prev*t,tokens);
            }

            case DIV: {
                tokens.poll();
                int t = parseFunction(tokens);
                return parseMultiplicationRec(prev/t,tokens);
            }

            default:
                return prev;
        }
    }

    private static int parseFunction(Deque<Token> tokens) throws UnexpectedTokenException {
        int t = parsePreFunction(tokens);
        return parseFunctionRec(t,tokens);
    }

    private static int parseFunctionRec(int prev, Deque<Token> tokens) throws UnexpectedTokenException {
        //F is F(T,T) or T, where T is terminal
        if (tokens.isEmpty())
            return prev;

        Token nextTok = tokens.peek();
        switch(nextTok.tokType) {

            case POW: {
                tokens.poll();
                int t = parsePreFunction(tokens);
                return parseFunctionRec((int)Math.pow(prev,t),tokens);
            }

            default:
                return prev;
        }
    }
    private static int parsePreFunction(Deque<Token> tokens) throws UnexpectedTokenException {
        //P is N*N or (A)*(A) or T, where N is number and A is a nested expression and T is terminal
        //or P is N*2 or (A)*2 or T, where N is number and A is a nested expression and T is terminal
        Token nextTok = tokens.peek();

        switch (nextTok.tokType) {
            case SQUARE:
                tokens.poll();
                int res1 = parseAddition(tokens);
                return res1*res1;
            case DOUBLENUM:
                tokens.poll();
                int res2 = parseAddition(tokens);
                return res2*2;
            default:
                return parseTerminal(tokens);
        }
    }

    private static int parseTerminal(Deque<Token> tokens) throws UnexpectedTokenException {
        //T is N or (A), where N is number and A is a nested expression
        Token tok = tokens.poll();
        switch (tok.tokType) {
            case NUMBER:
                return Integer.parseInt(tok.data.toString());

            case OPEN:
                int res = parseAddition(tokens);
                Token nextToken = tokens.poll();
                if( TokenType.CLOSE != nextToken.tokType)
                    throw new UnexpectedTokenException(nextToken);
                return res;

            default:
                throw new UnexpectedTokenException(tok);
        }
    }

    private SyntaxParser() { }
}
