package com.mridang.saavync.interceptors;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.protocol.HttpContext;

/*
 * This class is a custom HTTP response intercepter that will decompress
 * the GZIPped response
 */
public class GzippedInterceptor implements HttpResponseInterceptor {

    /*
     * @see org.apache.http.HttpResponseInterceptor#process(org.apache.http.
     * HttpResponse, org.apache.http.protocol.HttpContext)
     */
    @Override
    public void process(HttpResponse hreResponse, HttpContext hctContext)   throws HttpException, IOException {

        HttpEntity entity = hreResponse.getEntity();

        if (entity != null) {

            Header ceheader = entity.getContentEncoding();

            if (ceheader != null) {

                HeaderElement[] codecs = ceheader.getElements();

                for (int i = 0; i < codecs.length; i++) {

                    if (codecs[i].getName().equalsIgnoreCase("gzip")) {

                        hreResponse.setEntity(new HttpEntityWrapper(entity) {

                            @Override
                            public InputStream getContent() throws IOException, IllegalStateException {

                                return new GZIPInputStream(wrappedEntity.getContent());

                            }

                            @Override
                            public long getContentLength() {

                                return -1;

                            }

                        });

                        return;

                    }

                }

            }

        }

    }

}