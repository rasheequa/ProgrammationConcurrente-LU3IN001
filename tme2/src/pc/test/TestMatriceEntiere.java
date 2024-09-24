package pc.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import pc.MatriceEntiere;
import pc.TaillesNonConcordantesException;

public class TestMatriceEntiere {

	@Test
	public void testOriginal() {

		// -------------------- Test initialisation et affichage

		try {
			MatriceEntiere m1 = MatriceEntiere.parseMatrix(new File("data/donnees_somme1"));

			MatriceEntiere m2 = MatriceEntiere.parseMatrix(new File("data/donnees_produit1"));

			MatriceEntiere m3 = MatriceEntiere.parseMatrix(new File("data/donnees_somme2"));
			
			MatriceEntiere m4 = MatriceEntiere.parseMatrix(new File("data/donnees_produit2"));
			

			// utilise toString
			System.out.println("------------------ matrice 1 ------------------\n" + m1);
			System.out.println("------------------ matrice 2 ------------------\n" + m2);



			// -------------------- Test produit scalaire
			{
				System.out.println("------------------ Test scalaire ------------------");
				MatriceEntiere mat = m2.produitParScalaire(3);
				assertEquals(5, mat.nbLignes());
				assertEquals(4, mat.nbColonnes());
				int[] tab = { 3, 6, 9, 12, 15, 18, 21, 24, 27, 30, 33, 36, 39, 42, 45, 48, 51, 54, 57, 60 };
				checkValues(tab, mat);
				System.out.println(mat);
			}
			
			// -------------------- Test produit
			System.out.println("------------------ Test produit 1 ------------------");
			System.out.println(m2.toString() + "*\n" + m4.toString() + "=\n");
			try {
				MatriceEntiere mat = m2.produit(m4);
				assertEquals(5, mat.nbLignes());
				assertEquals(8, mat.nbColonnes());
				int[] tab = { -171, 64, -367, -196, 9, 73, -36, 72, -359, 160, -763, -360, -15, 201, -116, 200, -547,
						256, -1159, -524, -39, 329, -196, 328, -735, 352, -1555, -688, -63, 457, -276, 456, -923, 448,
						-1951, -852, -87, 585, -356, 584 };
				checkValues(tab, mat);
				System.out.println(mat);
			} catch (TaillesNonConcordantesException e) {
				System.out.println(e.getMessage());
				fail("Exception non attendue");
			}
			System.out.println("------------------ Test produit 2 ------------------");
			System.out.println(m2.toString() + "*\n" + m3.toString() + "=\n");
			try {
				System.out.println(m2.produit(m3));
				fail("Exception attendue");
			} catch (TaillesNonConcordantesException e) {
				// on attend bien une exception
				System.out.println(e.getMessage());
			}
			
			// -------------------- Test produit thread
			System.out.println("------------------ Test produit thread 1 ------------------");
			System.out.println(m2.toString() + "*\n" + m4.toString() + "=\n");
			try {
				MatriceEntiere mat = m2.produitMT(m4);
				assertEquals(5, mat.nbLignes());
				assertEquals(8, mat.nbColonnes());
				int[] tab = { -171, 64, -367, -196, 9, 73, -36, 72, -359, 160, -763, -360, -15, 201, -116, 200, -547,
						256, -1159, -524, -39, 329, -196, 328, -735, 352, -1555, -688, -63, 457, -276, 456, -923, 448,
						-1951, -852, -87, 585, -356, 584 };
				checkValues(tab, mat);
				System.out.println(mat);
			} catch (TaillesNonConcordantesException e) {
				System.out.println(e.getMessage());
				fail("Exception non attendue");
			}
			System.out.println("------------------ Test produit thread 2 ------------------");
			System.out.println(m2.toString() + "*\n" + m3.toString() + "=\n");
			try {
				System.out.println(m2.produitMT(m3));
				fail("Exception attendue");
			} catch (TaillesNonConcordantesException e) {
				// on attend bien une exception
				System.out.println(e.getMessage());
			}
						
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	public void testSerialisation(MatriceEntiere mat) throws FileNotFoundException, IOException {
		FileOutputStream fos = new FileOutputStream(new File("data/test1"));
		fos.write(mat.toString().getBytes());
		fos.close();
		MatriceEntiere mat2 = MatriceEntiere.parseMatrix(new File("data/test1"));
		assertTrue(mat.equals(mat2));
	}

	private void checkValues(int[] tab, MatriceEntiere mat) {
		for (int i = 0; i < mat.nbLignes() * mat.nbColonnes(); i++) {
			assertEquals(tab[i], mat.getElem(i / mat.nbColonnes(), i % mat.nbColonnes()));
		}
	}

}

