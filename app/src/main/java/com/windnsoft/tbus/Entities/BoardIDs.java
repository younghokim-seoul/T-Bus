package com.windnsoft.tbus.Entities;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Windn(Windnsoft@gmail.com) on 2017.
 */

public class BoardIDs {
    public final static int Free = 1;
    public final static int Bus = 2;
    public final static int Traction = 4;
    public final static int My = 9;
    public final static int Deals = 11;
    public final static int Interdiction = 12;
    public final static int Dispense = 13;
    public final static int News = 14;
    public final static int Traffic = 15;
    public final static int Festival = 16;
    public final static int Estimate = 110;
    public final static int Mountain = 120;

    public final static Map<Integer, BoardEntity> BoardMap = new HashMap<Integer, BoardEntity>() {
        {
            put(Free, new BoardEntity(Free, "bfree", "bfreere", "자유게시판"));
            put(Bus, new BoardEntity(Bus, "bbus", "bbusre", "관광버스"));
            put(Estimate, new BoardEntity(Estimate, "estimate", "estimatere", "버스견적"));
            put(Traction, new BoardEntity(Traction, "btraction", "btractionre", "긴급견인"));
            put(My, new BoardEntity(My, "", "", "내가쓴글"));
            put(Deals, new BoardEntity(Deals, "bdeals", "bdealsre", "직거래장터"));
            put(Interdiction, new BoardEntity(Interdiction, "binterdiction", "binterdictionre", "단속공유"));
            put(Dispense, new BoardEntity(Dispense, "bdispense", "bdispensere", "배차공차"));
            put(News, new BoardEntity(News, "bnews", "bnewsre", "업계뉴스"));
            put(Traffic, new BoardEntity(Traffic, "btraffic", "btrafficre", "교통정보"));
            put(Festival, new BoardEntity(Festival, "bfestival", "bfestivalre", "축제일정"));
            put(Mountain, new BoardEntity(Mountain, "mountain", "mountainre", "산행, 여행"));
        }
    };

    public static class BoardEntity {
        public int id;
        public String title;
        public String reply;
        public String text;

        BoardEntity(int id, String name, String replyName, String text) {
            this.id = id;
            this.title = name;
            this.reply = replyName;
            this.text = text;
        }
    }
}
