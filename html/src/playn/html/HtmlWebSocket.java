/**
 * Copyright 2010 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package playn.html;

import java.nio.ByteBuffer;

import com.google.gwt.typedarrays.client.ArrayBuffer;
import com.google.gwt.typedarrays.client.Uint8Array;

import playn.core.Net;

import playn.html.websocket.CloseEvent;
import playn.html.websocket.MessageEvent;
import playn.html.websocket.OpenEvent;
import playn.html.websocket.WebSocket;

public class HtmlWebSocket implements Net.WebSocket {

  private WebSocket ws;

  native byte[] toByteArray(Uint8Array bytes) /*-{
    return bytes;
  }-*/;

  HtmlWebSocket(String url, final Listener listener) {
    ws = WebSocket.create(url);
    ws.setListener(new WebSocket.Listener() {
      @Override
      public void onOpen(WebSocket socket, OpenEvent event) {
        listener.onOpen();
      }

      @Override
      public void onMessage(WebSocket socket, MessageEvent event) {
        // TODO(jgw): Differentiate binary and text messages.
        ArrayBuffer buf = event.getData();
        ByteBuffer bb = TypedArrayHelper.wrap(buf);
        listener.onDataMessage(bb);
      }

      @Override
      public void onClose(WebSocket socket, CloseEvent event) {
        listener.onClose();
      }
    });
  }

  @Override
  public void close() {
    ws.close();
  }

  @Override
  public void send(String data) {
    ws.send(data);
  }

  @Override
  public void send(ByteBuffer data) {
    int len = data.limit();
    // TODO(haustein) Sending the view directly does not work for some reason.
    // May be a chrome issue...?
    //  Object trick = data;
    // ArrayBufferView ta = ((HasArrayBufferView) trick).getTypedArray();
    // Int8Array view = Int8Array.create(ta.getBuffer(), ta.getByteOffset(), len)
    // ws.send(view);
    ArrayBuffer buf = ArrayBuffer.create(len);
    Uint8Array view = Uint8Array.create(buf);
    for (int i = 0; i < len; i++) {
      view.set(i, data.get(i));
    }
    ws.send(buf);
  }
}
