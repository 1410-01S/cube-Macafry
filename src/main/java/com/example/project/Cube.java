package com.example.project;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Cube {
	static ArrayList<String> undoStack = new ArrayList<>();

	/* Unindented comments represent different code-sections */
	public static void main(String[] args) {
		String[][][] Cube = createCube();

		if (args.length > 0) {
			// asuming each arg represents a move

			for (String move : args) {
				move(Cube, move);
			}

			showOfficial(Cube);

			return;
		} else {

			System.out.println(
					"No comandline args detected. Press \"m\" to input the moves manually or \"r\" to generate a randomized cube");

			Scanner sc = new Scanner(System.in);
			String mode = sc.next();
			if (mode.equalsIgnoreCase("m")) {
				userMoves(Cube, sc);
			} else if (mode.equalsIgnoreCase("r")) {
				System.out.println("Please enter the amount of random moves:");
				int moves = sc.nextInt();
				randomizedMoves(Cube, moves);
				showOfficial(Cube);

			} else {
				System.out.println("Unrecognized option, try again next time");
			}

			sc.close();
		}

		System.out.println("Steps to solve the cube:");
		for (String move : undoStack) {
			System.out.print(move + ", ");
		}
	}

	/* Set-up / IO functions */
	/*
	 * Understanding face relations:
	 * 
	 * using the following notation:
	 * [face on top]
	 * [face to the left][actual face][face to the right]
	 * [face below]
	 * 
	 * the faces are stored in the following orientations
	 * Y
	 * OBR
	 * W
	 * 
	 * Y
	 * GOB
	 * W
	 * 
	 * Y
	 * RGO
	 * W
	 * 
	 * Y
	 * BRG
	 * W
	 * 
	 * B
	 * OWR
	 * G
	 * 
	 * G
	 * OYR
	 * B
	 * 
	 * Why? - So that I can show the following format easily (That's the orientation
	 * of the cube in the readme)
	 * Y
	 * OBRG
	 * W
	 * 
	 * I tried looking for a reference example where I could apriciate how the faces
	 * are suposed to be oriented
	 * But I didn't find anything, so I'm choosing my own rotations
	 */
	static String[][][] createCube() {
		String[] faceColors = { "r", "b", "o", "g", "y", "w" };
		String[][][] faces = new String[6][3][3];

		for (int face = 0; face < faces.length; face++)
			for (int row = 0; row < faces[0].length; row++)
				for (int col = 0; col < faces[0][0].length; col++) {

					faces[face][row][col] = faceColors[face] + row + col;

				}

		return faces;
	}

	static void showOfficial(String[][][] faces) {
		for (int face = 0; face < faces.length; face++) {

			for (int row = 0; row < faces[0].length; row++) {

				for (int col = 0; col < faces[0][0].length - 1; col++) {
					System.out.print(faces[face][row][col].charAt(0) + "|");
				}

				System.out.println(faces[face][row][faces[0][0].length - 1].charAt(0));
			}

			System.out.println();
		}
	}

	static void showUseful(String[][][] faces) {
		String padding = "              ";
		String lineBreak = "-----------";

		System.out.println("Showing Cube:");

		// printing the top / yellow face (index = 4)

		for (int row = 0; row < 3; row++) {

			System.out.print(padding);

			for (int col = 0; col < 3 - 1; col++) {
				System.out.print(faces[4][row][col] + "|");
			}

			System.out.println(faces[4][row][2]);
		}

		System.out.println(padding + lineBreak);

		// printing the orange, blue, red and green faces (2,1,0,3 respectively)
		int[] faceIndeces = { 2, 1, 0, 3 };
		for (int row = 0; row < 3; row++) {

			for (int i = 0; i < faceIndeces.length; i++) {
				int face = faceIndeces[i];
				for (int col = 0; col < 3 - 1; col++) {

					System.out.print(faces[face][row][col] + "|");
				}

				System.out.print(faces[face][row][2] + " | ");

			}
			System.out.println();
		}

		System.out.println(padding + lineBreak);

		// printing the bottom / white face (index = 5)

		for (int row = 0; row < 3; row++) {

			System.out.print(padding);

			for (int col = 0; col < 3 - 1; col++) {
				System.out.print(faces[5][row][col] + "|");
			}

			System.out.println(faces[5][row][2]);
		}
	}

	/* Auxiliary functions */
	static String[][] copyFace(String[][] Face) {
		String[][] tempFace = new String[3][3];

		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++)
				tempFace[i][j] = Face[i][j];

		return tempFace;
	}

	static void flipHorizontally(String[][] face) {
		String temp;
		for (int row = 0; row < 3; row++) {
			temp = face[row][0];
			face[row][0] = face[row][2];
			face[row][2] = temp;
		}
	}

	/* Move-components functions */
	/* This method rotates a face either clockwise or counterclockwise */
	static void rotateFace(String[][] face, boolean clockwise) {

		String[][] tempFace = copyFace(face);

		if (clockwise) {
			// order is Face[y][x] or Face[row][col]

			for (int i = 0; i < 3; i++)
				for (int j = 0; j < 3; j++)
					face[i][j] = tempFace[2 - j][i];

		} else {

			for (int i = 0; i < 3; i++)
				for (int j = 0; j < 3; j++)
					face[i][j] = tempFace[j][2 - i];

		}

	}

	/*
	 * this functions handels the edge rotations of the u move
	 * it rotates the orange, blue, red, and green faces' top rows clockwise
	 * 
	 * note: clockwise looking at it from the top
	 * note2: the orange, blue, red and green faces are correspond to faces[x] where
	 * x = 2,1,0,3 respectively
	 * note3: the top rows correspond to faces[x][0]
	 * note4: since we're rotating using 3,2,1,0 is more convenient and achieves the
	 * same result
	 * 
	 */
	static void rotateTop(String[][][] faces, boolean clockwise) {

		if (clockwise == false) {

			String[] temp = faces[0][0];

			for (int i = 0; i < 3; i++) {

				faces[i][0] = faces[i + 1][0];
			}

			faces[3][0] = temp;

		} else {

			rotateTop(faces, false);
			rotateTop(faces, false);
			rotateTop(faces, false);
		}
	}

	/*
	 * this functions handels the edge rotations of the d move
	 * it rotates the orange, blue, red, and green faces' bottom rows clockwise
	 * 
	 * note: clockwise looking at it from the bottom
	 * note2: notes2-4 from rotateTop
	 */
	static void rotateBottom(String[][][] faces, boolean clockwise) {

		if (clockwise == true) {

			String[] temp = faces[0][2];

			for (int i = 0; i < 3; i++) {

				faces[i][2] = faces[i + 1][2];
			}

			faces[3][2] = temp;

		} else {

			rotateBottom(faces, true);
			rotateBottom(faces, true);
			rotateBottom(faces, true);
		}
	}

	/*
	 * this method is a little bit more abstract, as it could handle either the r or
	 * b case
	 * depending on the list of indexes and some previous rotations (to "align" the
	 * rotated column)
	 * more info in the rlfb cases of the move function
	 * 
	 * in this case clockwise means either option 1 or option 2
	 * (i don't know if both r and b have the same clockwise-ness)
	 * the right edge is of the form faces[x][:][2] for the xth face
	 */
	static void rotateRight(String[][][] faces, int[] indexes, boolean clockwise) {
		if (clockwise == false) {

			String[] temp = new String[3];
			String[][] currFace, prevFace;

			// Storing the 0th edge
			currFace = faces[indexes[0]];
			for (int i = 0; i < 3; i++) {
				temp[i] = currFace[i][2];
			}

			// doing most of the rotation
			for (int i = 1; i < indexes.length; i++) {
				// easy face access
				prevFace = currFace;
				currFace = faces[indexes[i]];

				// rotation
				for (int row = 0; row < 3; row++) {
					prevFace[row][2] = currFace[row][2];

				}
			}

			// setting the final edge
			for (int i = 0; i < 3; i++) {
				currFace[i][2] = temp[i];
			}

		} else {

			rotateRight(faces, indexes, false);
			rotateRight(faces, indexes, false);
			rotateRight(faces, indexes, false);

		}
	}

	/*
	 * Similar logic to the rotateRight method
	 * the left edge is of the form faces[0][:][2] for the xth face
	 */
	static void rotateLeft(String[][][] faces, int[] indexes, boolean clockwise) {
		if (clockwise == true) {

			String[] temp = new String[3];
			String[][] currFace, prevFace;

			// Storing the 0th edge
			currFace = faces[indexes[0]];
			for (int i = 0; i < 3; i++) {
				temp[i] = currFace[i][0];
			}

			// doing most of the rotation
			for (int i = 1; i < indexes.length; i++) {
				// easy face access
				prevFace = currFace;
				currFace = faces[indexes[i]];

				// rotation
				for (int row = 0; row < 3; row++) {
					prevFace[row][0] = currFace[row][0];

				}
			}

			// setting the final edge
			for (int i = 0; i < 3; i++) {
				currFace[i][0] = temp[i];
			}

		} else {

			rotateLeft(faces, indexes, true);
			rotateLeft(faces, indexes, true);
			rotateLeft(faces, indexes, true);

		}
	}

	/* Move functions */
	static void move(String[][][] Cube, String input) {
		switch (input.toLowerCase()) {
			case "u":
				moveUp(Cube, true);
				undoStack.add("u'");
				break;

			case "u'":
				moveUp(Cube, false);
				undoStack.add("u");
				break;

			case "d":
				moveDown(Cube, true);
				undoStack.add("d'");
				break;

			case "d'":
				moveDown(Cube, false);
				undoStack.add("d");
				break;

			case "r":
				moveRight(Cube, true);
				undoStack.add("r'");
				break;

			case "r'":
				moveRight(Cube, false);
				undoStack.add("r");
				break;

			case "l":
				moveLeft(Cube, true);
				undoStack.add("l'");
				break;

			case "l'":
				moveLeft(Cube, false);
				undoStack.add("l");
				break;

			case "f":
				moveFront(Cube, true);
				undoStack.add("f'");
				break;

			case "f'":
				moveFront(Cube, false);
				undoStack.add("f");
				break;

			case "b":
				moveBack(Cube, true);
				undoStack.add("b'");
				break;

			case "b'":
				moveBack(Cube, false);
				undoStack.add("b");
				break;

			default:
				System.out.println("Unrecognized Move. Try again");
				break;

		}
	}

	static void moveDown(String[][][] Cube, boolean clockwise) {
		rotateFace(Cube[5], clockwise);
		rotateBottom(Cube, clockwise);
	}

	static void moveUp(String[][][] Cube, boolean clockwise) {
		rotateFace(Cube[4], clockwise);
		rotateTop(Cube, clockwise);
	}

	static void moveFront(String[][][] Cube, boolean clockwise) {

		if (clockwise == true) {
			// Rotating the Orange face 180 deg
			rotateFace(Cube[2], true);
			rotateFace(Cube[2], true);

			// and the yellow and white by 90 (cw and ccw respectively)
			rotateFace(Cube[4], true);
			rotateFace(Cube[5], false);

			// Actual rotation
			rotateLeft(Cube, new int[] { 0, 4, 2, 5 }, true);
			rotateFace(Cube[1], true);

			// Restoring faces
			rotateFace(Cube[2], true);
			rotateFace(Cube[2], true);

			rotateFace(Cube[4], false);
			rotateFace(Cube[5], true);

		} else {
			// I don't want to risk missing a true / false
			moveBack(Cube, true);
			moveBack(Cube, true);
			moveBack(Cube, true);
		}
	}

	static void moveLeft(String[][][] Cube, boolean clockwise) {
		// Aligning the green face so that all desired rotations happen along the same
		// column
		// achieved by a 180 deg rotation
		rotateFace(Cube[3], true);
		rotateFace(Cube[3], true);

		// Actual rotation
		rotateLeft(Cube, new int[] { 1, 4, 3, 5 }, true);
		rotateFace(Cube[2], true);

		// Restoring green face
		rotateFace(Cube[3], true);
		rotateFace(Cube[3], true);

		if (clockwise == true) {

		} else {
			// I don't want to risk missing a true / false
			moveBack(Cube, true);
			moveBack(Cube, true);
			moveBack(Cube, true);
		}
	}

	static void moveRight(String[][][] Cube, boolean clockwise) {
		// Aligning the green face so that all desired rotations happen along the same
		// column
		// achieved by a 180 deg rotation
		rotateFace(Cube[3], true);
		rotateFace(Cube[3], true);

		// Actual rotation
		rotateRight(Cube, new int[] { 1, 4, 3, 5 }, true);
		rotateFace(Cube[0], true);

		// Restoring green face
		rotateFace(Cube[3], true);
		rotateFace(Cube[3], true);

		if (clockwise == true) {

		} else {
			// I don't want to risk missing a true / false
			moveBack(Cube, true);
			moveBack(Cube, true);
			moveBack(Cube, true);
		}
	}

	static void moveBack(String[][][] Cube, boolean clockwise) {
		if (clockwise == true) {
			// Rotating the Orange face 180 deg
			rotateFace(Cube[2], true);
			rotateFace(Cube[2], true);

			// and the yellow and white by 90 (cw and ccw respectively)
			rotateFace(Cube[4], true);
			rotateFace(Cube[5], false);

			// Actual rotation
			rotateRight(Cube, new int[] { 0, 4, 2, 5 }, true);
			rotateFace(Cube[3], true);

			// Restoring faces
			rotateFace(Cube[2], true);
			rotateFace(Cube[2], true);

			rotateFace(Cube[4], false);
			rotateFace(Cube[5], true);

		} else {
			// I don't want to risk missing a true / false
			moveBack(Cube, true);
			moveBack(Cube, true);
			moveBack(Cube, true);
		}
	}

	/* Mode Functions */

	static void randomizedMoves(String[][][] Cube, int totalMoves) {
		// Set-up
		String[] posibleMoves = { "u", "u'", "d", "d'", "r", "r'", "l", "l'", "f", "f'", "b", "b'" };
		Random generator = new Random();
		String currentMove;

		System.out.print("Moves executed: ");

		// Moves
		for (int i = 0; i < totalMoves; i++) {
			currentMove = posibleMoves[generator.nextInt(12)];
			move(Cube, currentMove);

			System.out.print(currentMove + ", ");
		}

		System.out.println();
	}

	static void userMoves(String[][][] Cube, Scanner sc) {
		// set-up
		System.out.println("List of moves: u, d, r, l, f, b (')");
		System.out.println("Press q to exit");
		System.out.println("Input a move:");
		String currentMove = sc.next();

		// moves
		while (currentMove.equalsIgnoreCase("q") == false) {
			move(Cube, currentMove);
			showOfficial(Cube);
			System.out.print("Next:");
			currentMove = sc.next();
		}

	}

}
