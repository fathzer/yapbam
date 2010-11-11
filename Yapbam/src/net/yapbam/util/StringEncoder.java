package net.yapbam.util;

/**
*  Cette classe permet de coder une cha�ne de caract�res de fa�on
 *  � en supprimer un certain nombre de caract�res interdits.
 *  <BR>Le codage est r�alis� suivant un principe simple qui ne permet
 *  l'utilisation de caract�res interdits nombreux et totalement quelconques.
 *  Cependant, il s'est, jusqu'� pr�sent, av�r� suffisant.
 *  <BR>Le principe est le suivant :
 *  <BR>Les caract�res interdits sont cod�s sur deux caract�res, le premier
 *  caract�re (dit de codage) indique que ce qui suit est un code de caract�re interdit.
 *  Le code suivant est sp�cifique du caract�re; '0' pour le caract�re de codage,
 *  '1' pour le premier caract�re interdit ... et ainsi de suite.
 *  <BR>Il d�coule de ceci les limitations suivantes :
 *  <UL>
 *  <LI>Le caract�re de codage doit �tre diff�rent des caract�res interdits</LI>
 *  <LI>Les caract�res interdits doivent �tre diff�rents de '0' � 'n' (n �tant le
 *  nombre de caract�res interdits</LI>
 *  </UL>
 * @author Jean-Marc Astesana
 * <BR>License : GPL v3
 */
public class StringEncoder {

}
