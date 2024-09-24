package pc.countwords;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;



public class MatriceEntiere extends Thread {
	private int [][] matrice; // matrice à deux dimensions
	
	public MatriceEntiere(int ligne, int colonne) {
		
		matrice = new int [ligne][colonne]; // Crée un tableau d'entiers
        
		// Initialise chaque ligne de la matrice et la remplit avec des zéros
		for(int i = 0; i<nbLignes();i++) {
			for(int j = 0; j<nbColonnes();j++) {
				setElem(i,j,0);
			}				
		}
		
	}
	
	public int getElem(int lig, int col) {
		return matrice[lig][col]; // Elément de la matrice à une position donnée
	    
	}
	
	public void setElem(int lig, int col, int val) {
		matrice[lig][col]=val; // Attribut une valeur à un element de la matrice à une position donnée
		
	}
	
	 public int nbLignes() {
		 return matrice.length; // Retourne le nombre de lignes de la matrice
	 }
	 
	 public int nbColonnes() {
		 return matrice[0].length; // Retourne le nombre de colonnes de la matrice
	 }
	 
	// Méthode statique pour lire une matrice depuis un fichier
	 public static MatriceEntiere parseMatrix(File fichier) throws
	 IOException{
		 try (BufferedReader in = new BufferedReader ( new FileReader(fichier))){

			 // Lit le nombre de lignes et de colonnes depuis le fichier
			 int nblig = Integer.parseInt(in.readLine());
		     int nbcol = Integer.parseInt(in.readLine());
			 
			 // Crée une nouvelle matrice avec ces dimensions
			 MatriceEntiere mat = new MatriceEntiere(nblig,nbcol);
			 
			// Remplit la matrice en lisant les valeurs ligne par ligne
			 for(int i = 0; i < nblig; i++) {
				 String line = in.readLine();
				 String[] word = line.split(" ");
				 
				 for(int j = 0; j<nbcol; j++) {
					 mat.setElem(i, j, Integer.parseInt(word[j]));
				 }
			 }
			return mat;
		 }
		 catch (Exception e) { throw new IOException(e); // Lance une exception en cas d'erreur
         
		 }
		 
	 }

	 public MatriceEntiere produitParScalaire(int n){
		
		 MatriceEntiere mat = new MatriceEntiere(this.nbLignes(),this.nbColonnes());
		 
		 Thread [] th = new Thread[nbLignes()];
		 
		 for (int i = 0; i < this.nbLignes(); i++) {
			 final int ligne = i;	
			 th[i]= new Thread(()->{
				 for(int colonne = 0; colonne<this.nbColonnes();colonne++) {
					 int sum = this.getElem(ligne,colonne) * n  ;
					 mat.setElem(ligne,colonne,sum);
				 }
			});
			 
			th[i].start();
		 }
		 
		 for (int j = 0; j < this.nbLignes(); j++) {
				try {
					th[j].join();
					
				} catch (InterruptedException e) {
			        e.printStackTrace();
			    }
			}
		 return mat;
	 }
	 
	 public MatriceEntiere produitMT(MatriceEntiere m)throws TaillesNonConcordantesException{
		
		 if(this.nbColonnes()!= ( m.nbLignes())){
	            throw new TaillesNonConcordantesException("Les dimensions ne sont pas concordantes : "+this.nbLignes()+" != "+m.nbColonnes());
	        }
		 
		// Crée une nouvelle matrice pour stocker le résultat du produit
		 MatriceEntiere mat = new MatriceEntiere(this.nbLignes(),m.nbColonnes());
		 int sum = 0, k=0;
		 
		// Calcule le produit matriciel
		 for(int i=0; i<this.nbLignes();i++) {
			 for(int j = 0; j<m.nbColonnes();j++) {
				 for(k = 0; k<this.nbColonnes();k++) {
					sum += this.getElem(i,k) * m.getElem(k,j) ;
				 }
			 mat.setElem(i,j,sum);
			 }
		 }
		 return mat;
	 }
	 
	 // Retourne une représentation de la matrice sous forme de chaîne de caractères
	 public String toString() {
		 
		 String s = nbLignes() +"\n"+ nbColonnes()+"\n";
		 for(int[] mat : matrice) { // Parcours les lignes de la matrice
			 for(int val : mat) { // Parcours les colonnes de la matrice
				 s+= val+" "; 
			 } 
			 s += "\n"; // Retour à la ligne apres le parcours d'une colonne
		 }
		 
		  
		 return s;
	 }
	
}
