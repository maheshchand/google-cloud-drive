package com.google.api.services.google.cloud.drive;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A sample application that runs multiple requests against the Drive API. The requests this sample
 * makes are:
 * <ul>
 * <li>Does a resumable media upload</li>
 * <li>Updates the uploaded file by renaming it</li>
 * <li>Does a resumable media download</li>
 * <li>Does a direct media upload</li>
 * <li>Does a direct media download</li>
 * </ul>
 *
 * @author mahesh.yadav.107@gmail.com (Mahesh Chand)
 */
public class GoogleDrive {

  private static final String MIME_FOLDER = "application/vnd.google-apps.folder";

  /**
   * Be sure to specify the name of your application. If the application name is {@code null} or
   * blank, the application will log a warning. Suggested format is "MyCompany-ProductName/1.0".
   */
  private static final String APPLICATION_NAME = "Google cloud drive -Mahesh";

  /**
   * Global instance of the {@link DataStoreFactory}. The best practice is to make it a single
   * globally shared instance across your application.
   */
  private static FileDataStoreFactory dataStoreFactory;

  /** Global instance of the HTTP transport. */
  private static HttpTransport httpTransport;

  /** Global instance of the JSON factory. */
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

  /** Global Drive API client. */
  private static Drive drive;

  /**
   * Authorizes the installed application to access user's protected data
   * 
   * @return AuthorizationCodeInstalledApp
   * @throws Exception
   */
  private static Credential authorize() throws Exception {

    // load client secrets
    GoogleClientSecrets clientSecrets =
        GoogleClientSecrets.load(JSON_FACTORY,
            new InputStreamReader(GoogleDrive.class.getResourceAsStream("/client_secrets.json")));
    if (clientSecrets.getDetails().getClientId().startsWith("Enter")
        || clientSecrets.getDetails().getClientSecret().startsWith("Enter ")) {
      System.out
          .println("Enter Client ID and Secret from https://code.google.com/apis/console/?api=drive "
              + "into /src/main/resources/client_secrets.json");
      System.exit(1);
    }
    // set up authorization code flow
    GoogleAuthorizationCodeFlow flow =
        new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets,
            Collections.singleton(DriveScopes.DRIVE)).setDataStoreFactory(dataStoreFactory).build();
    // authorize
    return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
  }

  private static void prepareDrive() {
    try {

      /** Directory to store user credentials. */
      java.io.File DATA_STORE_DIR =
          new java.io.File(System.getProperty("user.home"), ".store/drive_sample");

      httpTransport = GoogleNetHttpTransport.newTrustedTransport();
      dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);

      // authorization
      Credential credential = authorize();

      // set up the global Drive instance
      drive =
          new Drive.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName(
              APPLICATION_NAME).build();

      // For Service account
      /** Email of the Service Account */
      // private static final String SERVICE_ACCOUNT_EMAIL =
      // "634834838248-bkvm7it92gsgkduv3jlbnu830f8srr4c@developer.gserviceaccount.com";

      /** Path to the Service Account's Private Key file */
      // private static final String SERVICE_ACCOUNT_PKCS12_FILE_PATH =
      // "/tmp/fa484c86f9afaf3f6f95eed33bf5d777398bda47-fa484c86f9af.p12";


      // end service account

      // Service account

      // HttpTransport httpTransport = new NetHttpTransport();
      // JacksonFactory jsonFactory = new JacksonFactory();
      // GoogleCredential credential =
      // new GoogleCredential.Builder()
      // .setTransport(httpTransport)
      // .setJsonFactory(jsonFactory)
      // .setServiceAccountId(SERVICE_ACCOUNT_EMAIL)
      // .setServiceAccountScopes(Collections.singleton(DriveScopes.DRIVE_FILE))
      // .setServiceAccountPrivateKeyFromP12File(
      // new java.io.File(SERVICE_ACCOUNT_PKCS12_FILE_PATH)).build();
      // drive =
      // new Drive.Builder(httpTransport, jsonFactory, credential).setApplicationName(
      // APPLICATION_NAME).build();

      // For service account
      // run commands

      // View.header1("Starting Resumable Media Upload");
      // File uploadedFile = uploadFile(false);
      //
      // View.header1("Updating Uploaded File Name");
      // File updatedFile = updateFileWithTestSuffix(uploadedFile.getId());
      //
      // View.header1("Starting Resumable Media Download");
      // downloadFile(false, updatedFile);

      // View.header1("Starting Simple Media Upload");
      // uploadedFile = uploadFile(true);
      // uploadFile(true, "text/plain");
      // View.header1("Starting Simple Media Download");
      // downloadFile(true, uploadedFile);

      // View.header1("Success!");
    } catch (IOException e) {
      System.err.println(e.getMessage());
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  /**
   * Uploads a file using either resumable or direct media upload
   * 
   * @param java.io.File fileEntry
   * @param String uploadDir
   * @param boolean useDirectUpload
   * @param String mimeType
   * @return com.google.api.services.drive.model.File
   * @throws IOException
   */
  public static File uploadFile(java.io.File fileEntry, String uploadFolderId,
      boolean useDirectUpload, String mimeType) throws IOException {

    // Prepare and authorize drive
    prepareDrive();

    // Prepare file meta data
    File fileMetadata = new File();
    fileMetadata.setTitle(fileEntry.getName());
    fileMetadata.setMimeType(mimeType);

    FileContent mediaContent = new FileContent(mimeType, fileEntry);

    // Set upload directory if requested
    if (uploadFolderId != null) {

      // Get upload folder ID
      // String uploadFolderId = getGoogleDriveFolderId("RaspberryPi");
      // if (uploadFolderId != null)
      fileMetadata.setParents(Arrays.asList(new ParentReference().setId(uploadFolderId)));
    }

    // Prepare for file upload
    Drive.Files.Insert insert = drive.files().insert(fileMetadata, mediaContent);

    // Set upload properties and force progress listener
    MediaHttpUploader uploader = insert.getMediaHttpUploader();
    uploader.setDirectUploadEnabled(useDirectUpload);
    uploader.setProgressListener(new FileUploadProgressListener());

    // Upload takes place here
    com.google.api.services.drive.model.File file = insert.execute();
    return file;
  }

  /**
   * 
   * @param folderName
   * @return com.google.api.services.drive.model.File
   */
  public static File createFolder(String folderName, String parentFolderID) {

    com.google.api.services.drive.model.File file = null;

    // Prepare and authorize drive
    prepareDrive();

    try {
      // Create Folder if it doesn't exists

      File body = new File();
      body.setTitle(folderName);
      body.setMimeType(MIME_FOLDER);

      if (parentFolderID != null) {
        body.setParents(Arrays.asList(new ParentReference().setId(parentFolderID)));
      }

      file = drive.files().insert(body).execute();


    } catch (IOException e) {
      // TODO: error message here;
    }
    return file;
  }

  /**
   * Get folderId for a google drive folder
   * 
   * @param uploadDir
   * @return String
   * @throws IOException
   */
  public static String getGoogleDriveFolderId(String uploadDir) throws IOException {

    // Prepare and authorize drive
    prepareDrive();

    List<File> result = new ArrayList<File>();
    Files.List request = drive.files().list();

    request.setQ("title contains '" + uploadDir
        + "' and mimeType = 'application/vnd.google-apps.folder'");
    request.setFields("items(id,mimeType)");

    do {
      try {
        FileList files = request.execute();

        result.addAll(files.getItems());
        request.setPageToken(files.getNextPageToken());
      } catch (IOException e) {
        System.out.println("An error occurred: " + e);
        request.setPageToken(null);
      }
    } while (request.getPageToken() != null && request.getPageToken().length() > 0);

    if (result.isEmpty()) return null;

    // We just need one folder matching our upload directory
    return result.get(0).getId();
  }
  /** Downloads a file using either resumable or direct media download. */
  // private static void downloadFile(boolean useDirectDownload, File uploadedFile) throws
  // IOException {
  // // create parent directory (if necessary)
  // java.io.File parentDir = new java.io.File(DIR_FOR_DOWNLOADS);
  // if (!parentDir.exists() && !parentDir.mkdirs()) {
  // throw new IOException("Unable to create parent directory");
  // }
  // OutputStream out = new FileOutputStream(new java.io.File(parentDir, uploadedFile.getTitle()));
  //
  // MediaHttpDownloader downloader =
  // new MediaHttpDownloader(httpTransport, drive.getRequestFactory().getInitializer());
  // downloader.setDirectDownloadEnabled(useDirectDownload);
  // downloader.setProgressListener(new FileDownloadProgressListener());
  // downloader.download(new GenericUrl(uploadedFile.getDownloadUrl()), out);
  // }
}
