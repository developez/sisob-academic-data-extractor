/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/*
    Copyright (c) 2014 "(IA)2 Research Group. Universidad de Málaga"
                        http://iaia.lcc.uma.es | http://www.uma.es
    This file is part of SISOB Data Extractor.
    SISOB Data Extractor is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
    SISOB Data Extractor is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
    You should have received a copy of the GNU General Public License
    along with SISOB Data Extractor. If not, see <http://www.gnu.org/licenses/>.
*/

package eu.sisob.uma.api.googledrive;

/* Copyright (c) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gdata.client.docs.DocsService;
import com.google.gdata.client.media.ResumableGDataFileUploader;
import com.google.gdata.data.Link;
import com.google.gdata.data.docs.DocumentListEntry;
import com.google.gdata.data.docs.DocumentListFeed;
import com.google.gdata.data.media.MediaFileSource;
import com.google.gdata.util.ServiceException;
import com.google.gdata.client.uploader.FileUploadData;
import com.google.gdata.client.uploader.ProgressListener;
import com.google.gdata.client.uploader.ResumableHttpFileUploader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.log4j.Logger;

/**
 * A console aplication to demonstrate interaction with Google Docs API to
 * upload/update large media files using Resumable Upload protocol. 
 *
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/) - dlopezgonzalez@gmail.com 
 */
public class DocumentUploader {

  /** Default document list feed url. */
  public static final String DEFAULT_DOCLIST_FEED_URL =
      "https://docs.google.com/feeds/default/private/full";

  /** Default create-media-url for uploading documents */
  public static final String DEFAULT_RESUMABLE_UPLOAD_URL =
      "https://docs.google.com/feeds/upload/create-session/default/private/full";

  /** Maximum number of concurrent uploads */
  public static final int MAX_CONCURRENT_UPLOADS = 10;

  /** Time interval at which upload task will notify about the progress */
  public static final int PROGRESS_UPDATE_INTERVAL = 1000;

  /** Max size for each upload chunk */
  public static final int DEFAULT_CHUNK_SIZE = 512;//10000000;

  /** Instance of {@link DocumentList} */
  //private final DocumentList docs;
  private final DocsService service;


  /**
   * Constructor.
   *
   * @param docs {@link DocumentList} for interface to DocList API service.
   * @param out printstream to output status messages to.
   */
  //DocumentResumableUploadDemo(DocumentList docs, PrintStream out) {
  public DocumentUploader(DocsService service) 
  {
    this.service = service;    
  }

  /**
   * Uploads given collection of files.  The call blocks until all uploads are
   * done.
   *
   * @param url create-session url for initiating resumable uploads for
   *            documents API.
   * @param files list of absolute filepaths to files to upload.
   * @param chunkSize max size of each upload chunk.
   */
  public Collection<DocumentListEntry> uploadFiles(String url, List<File> files, int chunkSize)
      throws IOException, ServiceException, InterruptedException {
    // Create a listener
    FileUploadProgressListener listener = new FileUploadProgressListener();
    // Pool for handling concurrent upload tasks
    ExecutorService executor =
        Executors.newFixedThreadPool(MAX_CONCURRENT_UPLOADS);
    // Create {@link ResumableGDataFileUploader} for each file to upload
    List<ResumableGDataFileUploader> uploaders = Lists.newArrayList();
    for (File file : files) {
      MediaFileSource mediaFile = getMediaFileSource(file);      
      ResumableGDataFileUploader uploader =
          new ResumableGDataFileUploader.Builder(
               service, new URL(url), mediaFile, null /*empty meatadata*/)
              .title(mediaFile.getName())
              .chunkSize(chunkSize).executor(executor)
              .trackProgress(listener, PROGRESS_UPDATE_INTERVAL)
              .requestType(ResumableGDataFileUploader.RequestType.INSERT)
              .build();
      uploaders.add(uploader);      
    }
    // attach the listener to list of uploaders
    listener.listenTo(uploaders);

    // Start the upload
    for (ResumableGDataFileUploader uploader : uploaders) {
      uploader.start();
    }

    // wait for uploads to complete
    while (!listener.isDone()) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException ie) {
        listener.printResults();
        throw ie; // rethrow
      }
    }

    // print upload results
    listener.printResults();

    // return list of uploaded entries
    return listener.getUploaded();
  }

  private MediaFileSource getMediaFileSource(File file) {    
    MediaFileSource mediaFile = new MediaFileSource(file,
        DocumentListEntry.MediaType.fromFileName(file.getName()).getMimeType());    
    return mediaFile;
  }
  
  /**
   * A {@link ProgressListener} implementation to track upload progress.
   * The listener can track multiple uploads at the same time.
   * Use {@link #isDone} to check if all uploads are completed and
   * use {@link #getUploaded} to access results of successful uploads.
   */
  private class FileUploadProgressListener implements ProgressListener {

    private Collection<ResumableGDataFileUploader> trackedUploaders
        = Lists.newArrayList();
    private int pendingRequests;
    Map<String, DocumentListEntry> uploaded = Maps.newHashMap();
    Map<String, String> failed = Maps.newHashMap();

    boolean processed;

    public FileUploadProgressListener() {
      this.pendingRequests = 0;
    }

    public void listenTo(Collection<ResumableGDataFileUploader> uploaders) {
      this.trackedUploaders.addAll(uploaders);
      this.pendingRequests = trackedUploaders.size();
    }

    public synchronized void progressChanged(ResumableHttpFileUploader uploader)
    {
      String fileId = ((FileUploadData) uploader.getData()).getFileName();
      switch(uploader.getUploadState()) {
        case COMPLETE:
        case CLIENT_ERROR:
          pendingRequests -= 1;
          ProjectLogger.LOGGER.info(fileId + ": Completed");
          break;
        case IN_PROGRESS:
          ProjectLogger.LOGGER.info(fileId + ":"
              + String.format("%3.0f", uploader.getProgress() * 100) + "%");
          break;
        case NOT_STARTED:
          ProjectLogger.LOGGER.info(fileId + ":" + "Not Started");
          break;
      }
    }

    public synchronized boolean isDone() {
      // not done if there are any pending requests.
      if (pendingRequests > 0) {
        return false;
      }
      // if all responses are processed., nothing to do.
      if (processed) {
        return true;
      }
      // check if all response streams are available.
      for (ResumableGDataFileUploader uploader : trackedUploaders) {
        if (!uploader.isDone()) {
          return false;
        }
      }
      // process all responses
      for (ResumableGDataFileUploader uploader : trackedUploaders) {
        String fileId = ((FileUploadData) uploader.getData()).getFileName();
        switch(uploader.getUploadState()) {
          case COMPLETE:
            try {
              DocumentListEntry entry =
                  uploader.getResponse(DocumentListEntry.class);
              uploaded.put(fileId, entry);
            } catch (IOException e) {
              failed.put(fileId, "Upload completed, but unexpected error "
                  + "reading server response");
            } catch (ServiceException e) {
              failed.put(fileId,
                  "Upload completed, but failed to parse server response");
            }
            break;
          case CLIENT_ERROR:
            failed.put(fileId, "Failed at " + uploader.getProgress());
            break;
        }
      }
      processed = true;
      ProjectLogger.LOGGER.info("All requests done");
      return true;
    }

    public synchronized Collection<DocumentListEntry> getUploaded() {
      if (!isDone()) {
        return null;
      }
      return uploaded.values();
    }

    public synchronized void printResults() {
      if (!isDone()) {
        return;
      }
      ProjectLogger.LOGGER.info("Result: " + uploaded.size() + ", " + failed.size());
      if (uploaded.size() > 0) {
        ProjectLogger.LOGGER.info(" Successfully Uploaded:");
        for (Map.Entry<String, DocumentListEntry> entry : uploaded.entrySet()) {
          printDocumentEntry(entry.getValue());
        }
      }
      if (failed.size() > 0) {
        ProjectLogger.LOGGER.info(" Failed to upload:");
        for (Map.Entry entry : failed.entrySet()) {
          ProjectLogger.LOGGER.info("  " + entry.getKey() + ":" + entry.getValue());
        }
      }
    }

    /**
     * Prints out the specified document entry.
     *
     * @param doc the document entry to print.
     */
    public void printDocumentEntry(DocumentListEntry doc) {
      StringBuffer buffer = new StringBuffer();

      buffer.append(" -- " + doc.getTitle().getPlainText() + " ");
      if (!doc.getParentLinks().isEmpty()) {
        for (Link link : doc.getParentLinks()) {
          buffer.append("[" + link.getTitle() + "] ");
        }
      }
      buffer.append(doc.getResourceId());

      ProjectLogger.LOGGER.info(buffer);
    }

  }


}
