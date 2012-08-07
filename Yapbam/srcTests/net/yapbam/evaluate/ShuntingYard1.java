package net.yapbam.evaluate;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Classe fournissant une impl�mentation de l'algorithme de Shunting-Yard
 * permettant de passer de la notation Infixe et la notation polonaise invers�e
 * (RPN)
 * 
 * exemple :
 * 
 * 2 + 3 * 4 => 2 3 4 * +
 * 
 * @author blemoine
 */
public class ShuntingYard1 {

	/**
	 * Map Indiquant la pr�c�dence de chaque op�rateur
	 */
	private static final Map<String, Integer> PRECEDENCES = new HashMap<String, Integer>();
	static {
		PRECEDENCES.put("*", 3);
		PRECEDENCES.put("/", 3);
		PRECEDENCES.put("+", 2);
		PRECEDENCES.put("-", 2);
	}
	
	private static final String TOKENS_DELIMITERS = "*/+- ()"; 

	/**
	 * Comparateur de pr�c�dence des op�rateurs (plus l'op�rateur est
	 * prioritaire, plus sa pr�c�dence est �l�v�e)
	 */
	private static final Comparator<String> PRECEDENCE_OPERATOR_COMPARATOR = new Comparator<String>() {
		@Override
		public int compare(final String token1, final String token2) {
			final Integer precedence1 = PRECEDENCES.get(token1);
			if (precedence1 == null) {
				throw new IllegalArgumentException(token1
						+ " n'est pas exploitable");
			}

			final Integer precedence2 = PRECEDENCES.get(token2);
			if (precedence2 == null) {
				throw new IllegalArgumentException(token2
						+ " n'est pas exploitable");
			}

			return precedence1.compareTo(precedence2);
		}

	};

	/**
	 * Parse une chaine de caract�res vers un tableau de token � exploiter.
	 * @param tokensString Une chaine de caract�res contenant une formule
	 * @return un tableau des tokens pars�s
	 */
	protected String[] getTokens(String tokensString) {
		StringTokenizer tokens = new StringTokenizer(tokensString, TOKENS_DELIMITERS, true);
		String[] array = new String[tokens.countTokens()];
		for (int i = 0; i < array.length; i++) {
			array[i] = tokens.nextToken();
		}
		return array;
	}

	/**
	 * Parse la chaine de caract�res pass�e en param�tre, devant repr�senter une
	 * formule infixe et renvoit la notation RPN (notation polonaise invers�e)
	 * pour cette formule.
	 * 
	 * Les diff�rents membres de la formule doivent �tre s�par�s par des espaces
	 * Les chiffres � virgule doivent �tre s�par�s par des . Le parser ne
	 * reconnait pas les op�rations "unaires" dans cette version, tel que ! et -5
	 * 
	 * par exemple : 2.3 + 3 * 4 => 2.3 3 4 * +
	 * 
	 * @param tokensString
	 *            la formule originale
	 * @return le formule en notation RPN
	 * @throws RuntimeException
	 *             si il y a un probl�me lors du parsing
	 */
	public String toRPN(String tokensString) {
		final String[] tokens = getTokens(tokensString);

		final StringBuilder output = new StringBuilder();
		final Deque<String> stack = new ArrayDeque<String>();

		for (final String token : tokens) {
			if (token.matches("[+/*-]")) {
				// Le token est un op�rateur
				while (!stack.isEmpty()
						&& !"(".equals(stack.peek())
						&& PRECEDENCE_OPERATOR_COMPARATOR.compare(token, stack
								.peek()) < 0) {
					final String tokenOperator = stack.pop();
					output.append(tokenOperator);
					output.append(" ");
				}
				stack.push(token);
			} else if (")".equals(token)) {
				// Le token est une parenth�se fermante
				while (!stack.isEmpty() && !"(".equals(stack.peek())) {
					final String tokenOperator = stack.pop();
					output.append(tokenOperator);
					output.append(" ");
				}
				final String tokenOperator = stack.pollFirst();
				if (!"(".equals(tokenOperator)) {
					throw new RuntimeException(tokenOperator
							+ " aurait du etre une parenthese");
				}
			} else if ("(".equals(token)) {
				// Le token est une parenth�se ouvrante
				stack.push(token);
			} else {
				// Le token est un op�rande
				output.append(token);
				output.append(" ");
			}
		}

		// On finit de vider la stack
		for (final String operatorRestant : stack) {
			output.append(operatorRestant);
			output.append(" ");
		}

		return output.toString();
	}

	/**
	 * Evalue une expression RPN s�par� par des espaces
	 * 
	 * @param rpn
	 *            l'expression RPN
	 * @return la valeur num�rique du r�sultat
	 */
	public double evaluateRPN(final String rpn) {
		final String[] tokensArray = rpn.split(" ");
		final Deque<Double> stack = new ArrayDeque<Double>();
		for (String token : tokensArray) {
			if (token.matches("[0-9.]+")) {
				stack.push(Double.parseDouble(token));
			} else {
				// le token est donc un op�rateur
				if (stack.size() < 2) {
					throw new IllegalArgumentException(rpn
							+ "n'est pas bien form�");
				}
				final Double right = stack.pop();
				final Double left = stack.pop();
				final Double result = applyOperator(token, right, left);
				stack.push(result);
			}
		}
		if (stack.size() == 1) {
			return stack.pop();
		}
		throw new IllegalArgumentException(stack + " n'est pas calculable");
	}

	/**
	 * Applique l'op�rateur aux 2 membres
	 * 
	 * @param operateur
	 *            l'op�rateur (+,*,/ ou -)
	 * @param left
	 *            le membre de gauche
	 * @param right
	 *            le membre de droite
	 * 
	 * @return le r�sultat de l'op�ration
	 * @throws IllegalArgumentException si l'op�rateur pass� n'en est pas un 
	 */
	private Double applyOperator(String operateur, final Double left,
			final Double right) {
		final Double result;
		if ("+".equals(operateur)) {
			result = left + right;
		} else if ("-".equals(operateur)) {
			result = left - right;
		} else if ("*".equals(operateur)) {
			result = left * right;
		} else if ("/".equals(operateur)) {
			result = left / right;
		} else {
			throw new IllegalArgumentException(operateur + " n'est pas reconnu");
		}
		return result;
	}
	
	public static void main(String[] args) {
		String[] tests = new String[]{"4+5", "2*(5+3)", "-4", "-5+-2"};
		ShuntingYard1 sy = new ShuntingYard1();
		for (String test : tests) {
			System.out.println ("Original: "+test);
			String rpn = sy.toRPN(test);
			System.out.println ("RPN: "+rpn);
			try {
				double evaluate = sy.evaluateRPN(rpn);
				System.out.println ("Result: "+evaluate);
			} catch (Throwable e) {
				System.out.println ("Result: "+e.getMessage());
			}
			System.out.println();
		}
	}
}

