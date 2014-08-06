package com.google.api.services.google.cloud.drive;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.tika.Tika;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * A sample application that runs uploads files from a directory to Google cloud drive. It allows:
 * <ul>
 * <li>Choosing directory to read</li>
 * <li>File name pattern</li>
 * <li>Upload directory on cloud drive</li>
 * </ul>
 *
 * @author mahesh.yadav.107@gmail.com (Mahesh Chand)
 */
public class Main {

  /**
   * Starting point of application
   * 
   * @param args
   */
  public static void main(String[] args) {

    /**
     * Defaults
     */
    String READ_DIR = null;
    String FILE_NAME_PATTERN = ".*"; // everything
    String UPLOAD_DIR = null;
    Boolean createFolderForUpload = false;

    /**
     * CLI Options
     */

    // create Options object
    Options options = new Options();

    // Add options to it
    options.addOption("h", false, "prints this help/usage message");
    options.addOption("r", true, "(mandatory) directory to read for fetching files to upload");
    options.addOption("p", true,
        "(optional) pattern to match files in read directory, otherwise upload all files");
    options.addOption("u", true,
        "(optional) Upload directory on Google drive, otherwise uses drive root");
    options.addOption("f", false,
        "(optional) Create additional directory on Google drive for uploads");

    /**
     * CLI Parser
     */
    CommandLineParser parser = new GnuParser();

    try {
      // parse the command line arguments
      CommandLine line = parser.parse(options, args);

      if (line.hasOption("r")) {

        // Read CLI arguments
        READ_DIR = line.getOptionValue("r");

        if (line.hasOption("p")) {
          FILE_NAME_PATTERN = line.getOptionValue("p");
        }

        if (line.hasOption("u")) {
          UPLOAD_DIR = line.getOptionValue("u");
        }

        if (line.hasOption("f")) {
          createFolderForUpload = true;
        }

      } else {
        // Print help message
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -jar <jar-for-this-application.jar> <options>", options);

        // Do not report error status if it was a request for help message only
        if (!line.hasOption("h")) {
          System.exit(1);
        }
      }

    } catch (ParseException exp) {
      // oops, something went wrong
      System.err.println("CLI Parsing failed. Reason: " + exp.getMessage());
    }

    View.header2("Application started");

    // Get List of files to upload
    View.header1("Getting files to process from " + READ_DIR + " with pattern " + FILE_NAME_PATTERN);

    File dirToRead = new File(READ_DIR);
    ArrayList<File> filesToUpload =
        FileProcessor.listFilesForFolder(dirToRead, false, FILE_NAME_PATTERN);

    View.header1(filesToUpload.size() + " file(s) will be processed");

    // For file mime type determination
    Tika tika = new Tika();

    // Create folder for upload
    String timeStamp = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH).format(new Date());

    // Get Upload folder ID from upload directory
    String uploadFolderId = null;
    if (UPLOAD_DIR != null) {
      View.header1("Upload directory - " + UPLOAD_DIR);

      try {
        uploadFolderId = GoogleDrive.getGoogleDriveFolderId("RaspberryPi");
      } catch (IOException e) {
        System.out.println("An error occurred: " + e);
      }
    }

    // Check to see if we need to create upload folders
    if (createFolderForUpload) {
      View.header1("You have updated to create additional folders for uploads");

      com.google.api.services.drive.model.File folder =
          GoogleDrive.createFolder(timeStamp, uploadFolderId);
      uploadFolderId = folder.getId();
    }

    // Read list of files to upload and upload
    for (final File fileEntry : filesToUpload) {

      java.io.File UPLOAD_FILE = new java.io.File(fileEntry.getAbsolutePath());

      try {

        // Determine file mime type
        String mimeType = tika.detect(UPLOAD_FILE);

        // upload file
        View.header1("Uploading " + fileEntry.getAbsolutePath());
        GoogleDrive.uploadFile(UPLOAD_FILE, uploadFolderId, true, mimeType);

        // delete from local system
        View.header1("Deleting " + fileEntry.getAbsolutePath() + " from local file system");
        // fileEntry.delete();

      } catch (IOException e) {
        // show a message for failure in upload
        View.header1("Error while uploading: " + e.getMessage());
      }
    }

    // Done, get out of system.
    View.header2("Application finished doing its work, exiting");
    System.exit(0);
  }
}
