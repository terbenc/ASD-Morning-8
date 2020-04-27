package app;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * This class takes care of rendering note related code
 */
public class NoteView {
        List<Note> notes;

        /**
         * Read notes from the disk on instantiation of the class
         */
        public NoteView() {
                this.notes = this.readNotesFromDisk();
        }

        public VBox createNoteView() {
                // Header should be moved to its own class or Main
                VBox header = this.createHeader();

                // Draw box in which all notes are rendered
                VBox notesBox = this.drawNotes();

                // Scroll pane so the overflow of extra notes provides nice scrollbar
                ScrollPane scrollPane = new ScrollPane(notesBox);
                scrollPane.fitToWidthProperty().set(true);


                return new VBox(header, scrollPane);
        }

        private VBox createHeader() {
                Label welcomeLabel = new Label("Your notes");
                welcomeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));

                // Underline under Your notes text
                Line line = new Line();
                line.setStartX(0.0f);
                line.setStartY(0.0f);
                line.setEndX(200.0f); // Change this value to make line longer/shorter
                line.setEndY(0.0f);

                VBox header = new VBox(welcomeLabel, line);
                header.setPadding(new Insets(30, 15, 0, 10));

                return header;
        }

        /**
         * This method will go over each note and make VBox for it and add it to the allNotesBox
         * Each note registers actions for edit/delete
         */
        private VBox drawNotes() {
                // Container box in which each note will be rendered
                VBox allNotesBox = new VBox(20);
                allNotesBox.setPadding(new Insets(10, 20, 20, 10));
                allNotesBox.setStyle("-fx-background-color: #E4E8F0");


                //  Iterate over each note in note list and create elements
                this.notes.forEach((Note note) -> {
                        Text titleText = new Text(note.title);
                        titleText.setFont(Font.font("Arial", FontWeight.BOLD, 16));

                        // Create drop down with options
                        MenuItem editButton = new MenuItem("Edit");
                        MenuItem deleteButton = new MenuItem("Delete");
                        MenuButton menuButton = new MenuButton("...", null, editButton, deleteButton);

                        // Anchor pane is used to make title appear on the left side and drop down on the right
                        AnchorPane noteHeader = new AnchorPane(titleText, menuButton);
                        AnchorPane.setTopAnchor(titleText, 0.0);
                        AnchorPane.setLeftAnchor(titleText, 0.0);
                        AnchorPane.setTopAnchor(menuButton, 0.0);
                        AnchorPane.setRightAnchor(menuButton, 0.0);

                        Text contentText = new Text(note.content);

                        VBox noteBox = new VBox(0, noteHeader, contentText, new Separator());
                        noteBox.setSpacing(10);

                        // Register button actions
                        editButton.setOnAction((e) -> this.showEditWindow(note, titleText, contentText));
                        deleteButton.setOnAction((e) -> this.deleteNote(note, allNotesBox, noteBox));

                        // add created note to the allNotesBox
                        allNotesBox.getChildren().addAll(noteBox);
                });


                return allNotesBox;
        }

        /**
         * To delete note, you must first remove note from the object state, then remove the box which is
         * rendered, and finally write changes to the disk so its persisted.
         */
        private void deleteNote(Note note, VBox box, VBox noteBox) {
                this.notes.remove(note);

                box.getChildren().remove(noteBox);

                this.writeNotesToDisk();
        }

        /**
         * To update note, pass new updated note and original title and content boxes.
         * This method will update contents of the text fields and write changes to the disk
         */
        private void updateNote(Note note, Text title, Text content) {
                title.setText(note.title);
                content.setText(note.content);

                this.writeNotesToDisk();
        }


        /**
         * Shows the EDIT NOTE popup. This action is registered in edit button in NoteBox
         * This will open new popup with text fields for editing and buttons for saving and
         * canceling. Saving and canceling will call their respective methods for handling
         * the action.
         *
         * @param note
         * @param title
         * @param content
         */
        private void showEditWindow(Note note, Text title, Text content) {
                // Create a new window
                Stage stage = new Stage();

                // Text field for editing title; set current note title as content of the field
                TextField titleField = new TextField();
                titleField.setText(note.title);

                // Text field for editing note content; set current note content as content of the field
                TextArea contentField = new TextArea();
                contentField.setText(note.content);

                // Create save button and register action for it
                Button saveButton = new Button("Save");
                saveButton.setOnAction((e) -> {
                        // Update Note object with new values and pass it to method for handling saving with
                        // original boxes
                        note.title = titleField.getText();
                        note.content = contentField.getText();

                        this.updateNote(note, title, content);

                        stage.close();
                });

                // Create cancel button and register action which will just close the window without making changes
                Button cancelButton = new Button("Cancel");
                cancelButton.setOnAction((e) -> {
                        stage.close();
                });

                HBox buttons = new HBox(saveButton, cancelButton);
                buttons.setAlignment(Pos.BOTTOM_RIGHT);

                VBox layout = new VBox(titleField, contentField, buttons);
                stage.setTitle("Edit note");
                stage.setScene(new Scene(layout, 450, 450));

                stage.show();
        }

        /**
         * Method for reading notes from the persisted file on the disk
         *
         * Each line in the file should represent a Note to be created
         */
        private List<Note> readNotesFromDisk() {
                List<Note> notes = new ArrayList<Note>();

                try {
                        // Maybe unncessary variable?
                        String file_content = "";

                        // Get Overview.txt from the disk
                        File temp = new File("Notepad/src/Overview.txt");
                        Scanner scanner = new Scanner(temp);

                        // Read each line and create a Note object for it
                        while (scanner.hasNextLine()) {
                                file_content = scanner.nextLine() + System.getProperty("line.separator");

                                Note note = new Note(file_content);

                                notes.add(note);
                        }

                } catch (IOException e) {
                        e.printStackTrace();
                }

                return notes;
        }

        /**
         * Method for saving current state of notes to the disk
         *
         * Each note will be serialized and appended to the string which
         * will finally be stored on the disk
         */
        private void writeNotesToDisk() {
                String file_content = "";

                for (Note note : this.notes) {
                        file_content = file_content + note.serialize();
                }

                try {
                        File temp = new File("Notepad/src/Overview.txt");
                        FileWriter writer = new FileWriter(temp, false);
                        writer.write(file_content);
                        writer.close();
                } catch (IOException e) {
                        e.printStackTrace();
                }
        }
}