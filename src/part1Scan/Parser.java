package part1Scan;

import part1Scan.enums.SexpTypeEnum;
import part1Scan.exception.IncompletenessException;
import part1Scan.exception.InvalidSexpException;
import part1Scan.exception.LispException;
import part1Scan.enums.PrimitiveEnum;
import part1Scan.utils.SymTableUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018/3/3.
 */
public class Parser {
    Map<String, Sexp> symTable;
    TokenHandler tokenHandler = null;
    public Parser(){
        symTable = SymTableUtil.getInstance();
    }

    public void reset(String input){
        tokenHandler = new TokenHandler(input);
    }

    public Sexp startParsing() throws LispException {
        Sexp ans= input();
        if(tokenHandler.ckNextToken()!=null){
            throw new InvalidSexpException("redundant symbols starting from "+tokenHandler.ckNextToken());
        }
        return ans;
    }

    public Sexp input() throws LispException{
        String token = tokenHandler.ckNextToken();
        if(token == null){throw new IncompletenessException("absence of right parenthesis");}
        if(token.equals("(")){
            tokenHandler.skipToken();
            if(tokenHandler.ckNextToken().equals(")")){
                tokenHandler.skipToken();
                return symTable.get("NIL");
            }
            Sexp left=null, right=null;
            left = input();
            String token2 = tokenHandler.ckNextToken();
            if(token2 == null){
                throw new IncompletenessException("abesence of right parenthesis");
            }
            else if(token2.equals(".")){
                tokenHandler.skipToken();
                right = input();
                String token3 = tokenHandler.ckNextToken();
                if(token3==null){
                    throw new IncompletenessException("absence of right parenthesis for closing dot notation");
                }
                else if(!token3.equals(")")){
                    throw new InvalidSexpException("redundant characters starting from "+token3+" for dot notation");
                }
                tokenHandler.skipToken();
            }
            else{
                right = input2();
            }
            return new Sexp(SexpTypeEnum.NONATOM.getType(), left, right);
        }
        else if(token.equals(")")){
            throw new InvalidSexpException("unexpected right parenthesis");
        }
        else if(token.equals(".")){
            throw new InvalidSexpException("unexpected dot");
        }
        else if(isInteger(token)){
            //sexpression type is integer
            Sexp result = new Sexp(SexpTypeEnum.NUMERIC.getType(), Integer.parseInt(token));
            tokenHandler.skipToken();
            return result;
        }
        else{
            //sexpression type is symbol
            validSymbol(token);
            if(!symTable.containsKey(token)){
                symTable.put(token, new Sexp(SexpTypeEnum.SYMBOL.getType(), token));
            }
            tokenHandler.skipToken();
            return symTable.get(token);
        }
    }

    public Sexp input2() throws LispException{
        String token = tokenHandler.ckNextToken();
        if(token == null){
            throw new IncompletenessException("absence of right parenthesis");
        }
        if(token.equals(")")){
            tokenHandler.skipToken();
            return symTable.get("NIL");
        }
        else{
            Sexp left=null, right=null;
            left = input();
            right = input2();
            return new Sexp(3, left, right);
        }
    }

    public boolean isInteger(String x) {
        char[] chars = x.toCharArray();
        for(int i = 0; i<chars.length; i++){
            if(i==0 && (chars[0] == '+' || chars[0] == '-')){
                    continue;
            }
            if(chars[i] >= '0' && chars[i] <= '9'){continue;}
            return false;
        }
        if(chars.length==1 && (chars[0]=='+'||chars[0]=='-')){
            return false;
        }
        return true;
    }

    public void validSymbol(String x) throws InvalidSexpException{
        char[] chars = x.toCharArray();
        if(Character.isDigit(chars[0])){
            throw new InvalidSexpException("The symbol "+ x + " starts with a digit");
        }
        for(int i =0; i<chars.length; i++){
            if(Character.isUpperCase(chars[i]) || Character.isDigit(chars[i])){
                continue;
            }
            throw new InvalidSexpException(x+" disobeys the atom form " +
                    "-- uppercase letters and integers are only accepted");
        }
    }

    private Sexp getFromSymtable(String x){
        return symTable.get(x);
    }

}
