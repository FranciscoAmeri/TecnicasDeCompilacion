// Generated from com\compilador\MiLenguaje.g4 by ANTLR 4.9.3
package com.compilador;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class MiLenguajeLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.9.3", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		PA=1, PC=2, CA=3, CC=4, LA=5, LC=6, PYC=7, COMA=8, IGUAL=9, MAYOR=10, 
		MAYOR_IGUAL=11, MENOR=12, MENOR_IGUAL=13, EQL=14, DISTINTO=15, SUM=16, 
		RES=17, MUL=18, DIV=19, MOD=20, OR=21, AND=22, NOT=23, FOR=24, WHILE=25, 
		IF=26, ELSE=27, INT=28, CHAR=29, DOUBLE=30, VOID=31, RETURN=32, ID=33, 
		INTEGER=34, DECIMAL=35, CHARACTER=36, COMENTARIO_LINEA=37, COMENTARIO_BLOQUE=38, 
		WS=39, OTRO=40, CLASS=41, NAMESPACE=42, PUBLIC=43, PRIVATE=44, PROTECTED=45, 
		PTR=46, PTR_OP=47, COLON=48;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"PA", "PC", "CA", "CC", "LA", "LC", "PYC", "COMA", "IGUAL", "MAYOR", 
			"MAYOR_IGUAL", "MENOR", "MENOR_IGUAL", "EQL", "DISTINTO", "SUM", "RES", 
			"MUL", "DIV", "MOD", "OR", "AND", "NOT", "FOR", "WHILE", "IF", "ELSE", 
			"INT", "CHAR", "DOUBLE", "VOID", "RETURN", "ID", "INTEGER", "DECIMAL", 
			"CHARACTER", "COMENTARIO_LINEA", "COMENTARIO_BLOQUE", "WS", "OTRO", "LETRA", 
			"DIGITO", "CLASS", "NAMESPACE", "PUBLIC", "PRIVATE", "PROTECTED", "PTR", 
			"PTR_OP", "COLON"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'('", "')'", "'['", "']'", "'{'", "'}'", "';'", "','", "'='", 
			"'>'", "'>='", "'<'", "'<='", "'=='", "'!='", "'+'", "'-'", null, "'/'", 
			"'%'", "'||'", "'&&'", "'!'", "'for'", "'while'", "'if'", "'else'", "'int'", 
			"'char'", "'double'", "'void'", "'return'", null, null, null, null, null, 
			null, null, null, "'class'", "'namespace'", "'public'", "'private'", 
			"'protected'", null, "'->'", "':'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "PA", "PC", "CA", "CC", "LA", "LC", "PYC", "COMA", "IGUAL", "MAYOR", 
			"MAYOR_IGUAL", "MENOR", "MENOR_IGUAL", "EQL", "DISTINTO", "SUM", "RES", 
			"MUL", "DIV", "MOD", "OR", "AND", "NOT", "FOR", "WHILE", "IF", "ELSE", 
			"INT", "CHAR", "DOUBLE", "VOID", "RETURN", "ID", "INTEGER", "DECIMAL", 
			"CHARACTER", "COMENTARIO_LINEA", "COMENTARIO_BLOQUE", "WS", "OTRO", "CLASS", 
			"NAMESPACE", "PUBLIC", "PRIVATE", "PROTECTED", "PTR", "PTR_OP", "COLON"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public MiLenguajeLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "MiLenguaje.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\62\u0139\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t"+
		" \4!\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t"+
		"+\4,\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\3\2"+
		"\3\2\3\3\3\3\3\4\3\4\3\5\3\5\3\6\3\6\3\7\3\7\3\b\3\b\3\t\3\t\3\n\3\n\3"+
		"\13\3\13\3\f\3\f\3\f\3\r\3\r\3\16\3\16\3\16\3\17\3\17\3\17\3\20\3\20\3"+
		"\20\3\21\3\21\3\22\3\22\3\23\3\23\3\24\3\24\3\25\3\25\3\26\3\26\3\26\3"+
		"\27\3\27\3\27\3\30\3\30\3\31\3\31\3\31\3\31\3\32\3\32\3\32\3\32\3\32\3"+
		"\32\3\33\3\33\3\33\3\34\3\34\3\34\3\34\3\34\3\35\3\35\3\35\3\35\3\36\3"+
		"\36\3\36\3\36\3\36\3\37\3\37\3\37\3\37\3\37\3\37\3\37\3 \3 \3 \3 \3 \3"+
		"!\3!\3!\3!\3!\3!\3!\3\"\3\"\5\"\u00cc\n\"\3\"\3\"\3\"\7\"\u00d1\n\"\f"+
		"\"\16\"\u00d4\13\"\3#\6#\u00d7\n#\r#\16#\u00d8\3$\3$\3$\3$\3%\3%\3%\3"+
		"%\5%\u00e3\n%\3%\3%\3&\3&\3&\3&\7&\u00eb\n&\f&\16&\u00ee\13&\3&\3&\3\'"+
		"\3\'\3\'\3\'\7\'\u00f6\n\'\f\'\16\'\u00f9\13\'\3\'\3\'\3\'\3\'\3\'\3("+
		"\3(\3(\3(\3)\3)\3*\3*\3+\3+\3,\3,\3,\3,\3,\3,\3-\3-\3-\3-\3-\3-\3-\3-"+
		"\3-\3-\3.\3.\3.\3.\3.\3.\3.\3/\3/\3/\3/\3/\3/\3/\3/\3\60\3\60\3\60\3\60"+
		"\3\60\3\60\3\60\3\60\3\60\3\60\3\61\3\61\3\62\3\62\3\62\3\63\3\63\3\u00f7"+
		"\2\64\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35"+
		"\20\37\21!\22#\23%\24\'\25)\26+\27-\30/\31\61\32\63\33\65\34\67\359\36"+
		";\37= ?!A\"C#E$G%I&K\'M(O)Q*S\2U\2W+Y,[-]._/a\60c\61e\62\3\2\7\5\2\f\f"+
		"\17\17))\4\2\f\f\17\17\5\2\13\f\17\17\"\"\4\2C\\c|\3\2\62;\2\u013e\2\3"+
		"\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2"+
		"\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31"+
		"\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2"+
		"\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2\2\2"+
		"\61\3\2\2\2\2\63\3\2\2\2\2\65\3\2\2\2\2\67\3\2\2\2\29\3\2\2\2\2;\3\2\2"+
		"\2\2=\3\2\2\2\2?\3\2\2\2\2A\3\2\2\2\2C\3\2\2\2\2E\3\2\2\2\2G\3\2\2\2\2"+
		"I\3\2\2\2\2K\3\2\2\2\2M\3\2\2\2\2O\3\2\2\2\2Q\3\2\2\2\2W\3\2\2\2\2Y\3"+
		"\2\2\2\2[\3\2\2\2\2]\3\2\2\2\2_\3\2\2\2\2a\3\2\2\2\2c\3\2\2\2\2e\3\2\2"+
		"\2\3g\3\2\2\2\5i\3\2\2\2\7k\3\2\2\2\tm\3\2\2\2\13o\3\2\2\2\rq\3\2\2\2"+
		"\17s\3\2\2\2\21u\3\2\2\2\23w\3\2\2\2\25y\3\2\2\2\27{\3\2\2\2\31~\3\2\2"+
		"\2\33\u0080\3\2\2\2\35\u0083\3\2\2\2\37\u0086\3\2\2\2!\u0089\3\2\2\2#"+
		"\u008b\3\2\2\2%\u008d\3\2\2\2\'\u008f\3\2\2\2)\u0091\3\2\2\2+\u0093\3"+
		"\2\2\2-\u0096\3\2\2\2/\u0099\3\2\2\2\61\u009b\3\2\2\2\63\u009f\3\2\2\2"+
		"\65\u00a5\3\2\2\2\67\u00a8\3\2\2\29\u00ad\3\2\2\2;\u00b1\3\2\2\2=\u00b6"+
		"\3\2\2\2?\u00bd\3\2\2\2A\u00c2\3\2\2\2C\u00cb\3\2\2\2E\u00d6\3\2\2\2G"+
		"\u00da\3\2\2\2I\u00de\3\2\2\2K\u00e6\3\2\2\2M\u00f1\3\2\2\2O\u00ff\3\2"+
		"\2\2Q\u0103\3\2\2\2S\u0105\3\2\2\2U\u0107\3\2\2\2W\u0109\3\2\2\2Y\u010f"+
		"\3\2\2\2[\u0119\3\2\2\2]\u0120\3\2\2\2_\u0128\3\2\2\2a\u0132\3\2\2\2c"+
		"\u0134\3\2\2\2e\u0137\3\2\2\2gh\7*\2\2h\4\3\2\2\2ij\7+\2\2j\6\3\2\2\2"+
		"kl\7]\2\2l\b\3\2\2\2mn\7_\2\2n\n\3\2\2\2op\7}\2\2p\f\3\2\2\2qr\7\177\2"+
		"\2r\16\3\2\2\2st\7=\2\2t\20\3\2\2\2uv\7.\2\2v\22\3\2\2\2wx\7?\2\2x\24"+
		"\3\2\2\2yz\7@\2\2z\26\3\2\2\2{|\7@\2\2|}\7?\2\2}\30\3\2\2\2~\177\7>\2"+
		"\2\177\32\3\2\2\2\u0080\u0081\7>\2\2\u0081\u0082\7?\2\2\u0082\34\3\2\2"+
		"\2\u0083\u0084\7?\2\2\u0084\u0085\7?\2\2\u0085\36\3\2\2\2\u0086\u0087"+
		"\7#\2\2\u0087\u0088\7?\2\2\u0088 \3\2\2\2\u0089\u008a\7-\2\2\u008a\"\3"+
		"\2\2\2\u008b\u008c\7/\2\2\u008c$\3\2\2\2\u008d\u008e\7,\2\2\u008e&\3\2"+
		"\2\2\u008f\u0090\7\61\2\2\u0090(\3\2\2\2\u0091\u0092\7\'\2\2\u0092*\3"+
		"\2\2\2\u0093\u0094\7~\2\2\u0094\u0095\7~\2\2\u0095,\3\2\2\2\u0096\u0097"+
		"\7(\2\2\u0097\u0098\7(\2\2\u0098.\3\2\2\2\u0099\u009a\7#\2\2\u009a\60"+
		"\3\2\2\2\u009b\u009c\7h\2\2\u009c\u009d\7q\2\2\u009d\u009e\7t\2\2\u009e"+
		"\62\3\2\2\2\u009f\u00a0\7y\2\2\u00a0\u00a1\7j\2\2\u00a1\u00a2\7k\2\2\u00a2"+
		"\u00a3\7n\2\2\u00a3\u00a4\7g\2\2\u00a4\64\3\2\2\2\u00a5\u00a6\7k\2\2\u00a6"+
		"\u00a7\7h\2\2\u00a7\66\3\2\2\2\u00a8\u00a9\7g\2\2\u00a9\u00aa\7n\2\2\u00aa"+
		"\u00ab\7u\2\2\u00ab\u00ac\7g\2\2\u00ac8\3\2\2\2\u00ad\u00ae\7k\2\2\u00ae"+
		"\u00af\7p\2\2\u00af\u00b0\7v\2\2\u00b0:\3\2\2\2\u00b1\u00b2\7e\2\2\u00b2"+
		"\u00b3\7j\2\2\u00b3\u00b4\7c\2\2\u00b4\u00b5\7t\2\2\u00b5<\3\2\2\2\u00b6"+
		"\u00b7\7f\2\2\u00b7\u00b8\7q\2\2\u00b8\u00b9\7w\2\2\u00b9\u00ba\7d\2\2"+
		"\u00ba\u00bb\7n\2\2\u00bb\u00bc\7g\2\2\u00bc>\3\2\2\2\u00bd\u00be\7x\2"+
		"\2\u00be\u00bf\7q\2\2\u00bf\u00c0\7k\2\2\u00c0\u00c1\7f\2\2\u00c1@\3\2"+
		"\2\2\u00c2\u00c3\7t\2\2\u00c3\u00c4\7g\2\2\u00c4\u00c5\7v\2\2\u00c5\u00c6"+
		"\7w\2\2\u00c6\u00c7\7t\2\2\u00c7\u00c8\7p\2\2\u00c8B\3\2\2\2\u00c9\u00cc"+
		"\5S*\2\u00ca\u00cc\7a\2\2\u00cb\u00c9\3\2\2\2\u00cb\u00ca\3\2\2\2\u00cc"+
		"\u00d2\3\2\2\2\u00cd\u00d1\5S*\2\u00ce\u00d1\5U+\2\u00cf\u00d1\7a\2\2"+
		"\u00d0\u00cd\3\2\2\2\u00d0\u00ce\3\2\2\2\u00d0\u00cf\3\2\2\2\u00d1\u00d4"+
		"\3\2\2\2\u00d2\u00d0\3\2\2\2\u00d2\u00d3\3\2\2\2\u00d3D\3\2\2\2\u00d4"+
		"\u00d2\3\2\2\2\u00d5\u00d7\5U+\2\u00d6\u00d5\3\2\2\2\u00d7\u00d8\3\2\2"+
		"\2\u00d8\u00d6\3\2\2\2\u00d8\u00d9\3\2\2\2\u00d9F\3\2\2\2\u00da\u00db"+
		"\5E#\2\u00db\u00dc\7\60\2\2\u00dc\u00dd\5E#\2\u00ddH\3\2\2\2\u00de\u00e2"+
		"\7)\2\2\u00df\u00e3\n\2\2\2\u00e0\u00e1\7^\2\2\u00e1\u00e3\13\2\2\2\u00e2"+
		"\u00df\3\2\2\2\u00e2\u00e0\3\2\2\2\u00e3\u00e4\3\2\2\2\u00e4\u00e5\7)"+
		"\2\2\u00e5J\3\2\2\2\u00e6\u00e7\7\61\2\2\u00e7\u00e8\7\61\2\2\u00e8\u00ec"+
		"\3\2\2\2\u00e9\u00eb\n\3\2\2\u00ea\u00e9\3\2\2\2\u00eb\u00ee\3\2\2\2\u00ec"+
		"\u00ea\3\2\2\2\u00ec\u00ed\3\2\2\2\u00ed\u00ef\3\2\2\2\u00ee\u00ec\3\2"+
		"\2\2\u00ef\u00f0\b&\2\2\u00f0L\3\2\2\2\u00f1\u00f2\7\61\2\2\u00f2\u00f3"+
		"\7,\2\2\u00f3\u00f7\3\2\2\2\u00f4\u00f6\13\2\2\2\u00f5\u00f4\3\2\2\2\u00f6"+
		"\u00f9\3\2\2\2\u00f7\u00f8\3\2\2\2\u00f7\u00f5\3\2\2\2\u00f8\u00fa\3\2"+
		"\2\2\u00f9\u00f7\3\2\2\2\u00fa\u00fb\7,\2\2\u00fb\u00fc\7\61\2\2\u00fc"+
		"\u00fd\3\2\2\2\u00fd\u00fe\b\'\2\2\u00feN\3\2\2\2\u00ff\u0100\t\4\2\2"+
		"\u0100\u0101\3\2\2\2\u0101\u0102\b(\2\2\u0102P\3\2\2\2\u0103\u0104\13"+
		"\2\2\2\u0104R\3\2\2\2\u0105\u0106\t\5\2\2\u0106T\3\2\2\2\u0107\u0108\t"+
		"\6\2\2\u0108V\3\2\2\2\u0109\u010a\7e\2\2\u010a\u010b\7n\2\2\u010b\u010c"+
		"\7c\2\2\u010c\u010d\7u\2\2\u010d\u010e\7u\2\2\u010eX\3\2\2\2\u010f\u0110"+
		"\7p\2\2\u0110\u0111\7c\2\2\u0111\u0112\7o\2\2\u0112\u0113\7g\2\2\u0113"+
		"\u0114\7u\2\2\u0114\u0115\7r\2\2\u0115\u0116\7c\2\2\u0116\u0117\7e\2\2"+
		"\u0117\u0118\7g\2\2\u0118Z\3\2\2\2\u0119\u011a\7r\2\2\u011a\u011b\7w\2"+
		"\2\u011b\u011c\7d\2\2\u011c\u011d\7n\2\2\u011d\u011e\7k\2\2\u011e\u011f"+
		"\7e\2\2\u011f\\\3\2\2\2\u0120\u0121\7r\2\2\u0121\u0122\7t\2\2\u0122\u0123"+
		"\7k\2\2\u0123\u0124\7x\2\2\u0124\u0125\7c\2\2\u0125\u0126\7v\2\2\u0126"+
		"\u0127\7g\2\2\u0127^\3\2\2\2\u0128\u0129\7r\2\2\u0129\u012a\7t\2\2\u012a"+
		"\u012b\7q\2\2\u012b\u012c\7v\2\2\u012c\u012d\7g\2\2\u012d\u012e\7e\2\2"+
		"\u012e\u012f\7v\2\2\u012f\u0130\7g\2\2\u0130\u0131\7f\2\2\u0131`\3\2\2"+
		"\2\u0132\u0133\7,\2\2\u0133b\3\2\2\2\u0134\u0135\7/\2\2\u0135\u0136\7"+
		"@\2\2\u0136d\3\2\2\2\u0137\u0138\7<\2\2\u0138f\3\2\2\2\n\2\u00cb\u00d0"+
		"\u00d2\u00d8\u00e2\u00ec\u00f7\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}