package com.m1miageprojet.tcpcommunication;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.m1miageprojet.chord.ChatRoom;
import com.m1miageprojet.chord.ChordPeer;

public class Request {
	private ChordPeer peer;

	public Request(ChordPeer peer) {
		this.peer = peer;
	}

	public void sendRequest(String req, ChordPeer distPeer) {
		sendRequest(req, peer, distPeer);
	}

	/**
	 * Send a message to a recipient
	 * 
	 * @param req
	 *            as a String
	 * @param sender
	 * @param distPeer
	 *            as the recipient
	 */
	public void sendRequest(String req, ChordPeer sender, ChordPeer distPeer) {
		DataSender ds = new DataSender();
		JSONObject jsonReq;
		try {
			jsonReq = new JSONObject(req);
		} catch (JSONException e1) {
			jsonReq = new JSONObject();
			try {
				jsonReq.put("expe", sender.toJSON(1));
				jsonReq.put("dest", distPeer.toJSON(1));
				if ("JOIN".equals(req)) {
					jsonReq.put("salons", peer.getGestionSalon().toJSON());
					jsonReq.put("req", "JOIN");
				} else if ("LEAVE".equals(req)) {
					jsonReq.put("req", "LEAVE");
					System.out.println("Requete: quitter le Chord ..");
					peer.leaveChord();
				} else if ("REP_JOIN".equals(req)) {
					jsonReq.put("salons", peer.getGestionSalon().toJSON());
					jsonReq.put("req", "REP_JOIN");
				}
				
			} catch (JSONException e2) {
				e2.printStackTrace();
			}
		}
		ds.send(jsonReq.toString().getBytes(), distPeer.getIp(), distPeer.getPort());
	}
	
	/**
	 * Send a message to a ChatRoom
	 * @param req
	 * @param distPeer
	 * @param keyChatRoom
	 */
	public void sendRequest(String req, ChordPeer distPeer, int keyChatRoom) {
		DataSender ds = new DataSender();
		JSONObject jsonReq = new JSONObject();
		try {
			jsonReq.put("dest", distPeer.toJSON(1));
			if ("JOIN_SALON".equals(req)) {
				jsonReq.put("keyChatRoom", keyChatRoom);
				jsonReq.put("req", "JOIN_SALON");
			} 
		} catch (JSONException e2) {
			e2.printStackTrace();
		}
		ds.send(jsonReq.toString().getBytes(), distPeer.getIp(), distPeer.getPort());
	}

	/**
	 * Send a message to a recipient
	 * 
	 * @param req
	 *            as a byte array
	 * @param distPeer
	 *            as the recipient
	 */
	public void sendRequest(byte[] req, ChordPeer distPeer) {
		DataSender ds = new DataSender();
		JSONObject jsonReq = new JSONObject();
		try {
			jsonReq.put("expe", peer.toJSON(1));
			jsonReq.put("dest", distPeer.toJSON(1));
			jsonReq.put("req", "chat");
			JSONArray jsonByteArray = new JSONArray();
			for (byte b : req) {
				jsonByteArray.put(b);
			}
			jsonReq.put("message", jsonByteArray);
			ds.send(jsonReq.toString().getBytes(), distPeer.getIp(), distPeer.getPort());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void processRequest(String req) {
		try {
			JSONObject jsonReq = new JSONObject(req);
			ChordPeer expe = new ChordPeer(jsonReq.getJSONObject("expe"));
			ChordPeer dest = new ChordPeer(jsonReq.getJSONObject("dest"));
			String reqName = jsonReq.getString("req");
			if (!reqName.equals("REP_JOIN") && !peer.equals(expe)) {
				
				if (reqName.equals("JOIN")) {
					
					ChatRoom chat = new ChatRoom (jsonReq.getJSONObject("salons").getJSONArray("salons").getJSONObject(0));//on recupere le salon crer par lexp
					peer.getGestionSalon().getChatRoomList().put(chat.getId(), chat);
					if (peer.equals(dest)) {
						sendRequest("REP_JOIN", expe);
						
					}
					expe.joinChord(peer);
					System.out.println("un peer vient de joindre le Chord avec l'Id: " + expe.getMyId());
					
				} else if (reqName.equals("LEAVE")) {
					if(peer.getMyId() == 78)
					{
						System.out.println("avant leave chord");
						System.out.println("pred : " + peer.getPred());
						System.out.println("succ : " + peer.getSucc());
						System.out.println(peer.findkey(expe.getMyId()));
						System.out.println("pred de " + peer.findkey(expe.getMyId())+ " : " + peer.findkey(expe.getMyId()).getPred().getMyId());
					}
					peer.findkey(expe.getMyId()).leaveChord();
					if(peer.getMyId() == 78)
					{
						System.out.println("apres leave chord");
						System.out.println("pred : " + peer.getPred());
						System.out.println("succ : " + peer.getSucc());
					}
					System.out.println("un peer " + expe.getMyId() + " vient de quitter le Chord");
					
				} else if (reqName.equals("chat")) {
					
					JSONArray jsonByteArray = jsonReq.getJSONArray("message");
					byte[] msgByteArray = new byte[jsonByteArray.length()];
					for (int i = 0; i < jsonByteArray.length(); i++) {
						msgByteArray[i] = Byte.parseByte(jsonByteArray.getString(i));
					}
					System.out.println(new String(msgByteArray));
				}

				else if(reqName.equals("JOIN_SALON")){
					
					
					
				}
				peer.forwardMessage(req, expe);
			} else if (reqName.equals("REP_JOIN")) {

				System.out.println("Requete: jointure du pair " + peer.getMyId());
				peer.joinChord(expe);
				peer.getGestionSalon().setChatRoom(jsonReq.getJSONObject("salons"));
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
