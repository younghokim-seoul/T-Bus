package com.windnsoft.tbus.General;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.CommonDataKinds.Website;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Groups;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by Windn(Windnsoft@gmail.com) on 2016.
 */
public class PhonebookReader {
    final private Context ctx;

    private boolean needMobPhone = false;

    /**
     * 중복또한 제거한다. [null이나 빈값을 포함할경우 중복을 제거하면 기준이 애매해지기 때문에 이걸 사용할 때만 제거한다.]
     */
    public void needMobilePhone() {
        needMobPhone = true;
    }

    private boolean useEmail = false;

    public void useEmail() {
        useEmail = true;
    }

    private boolean useAddr = false;

    public void useAddr() {
        useAddr = true;
    }

    private boolean useMemo = false;

    public void useMemo() {
        useMemo = true;
    }

    private boolean useCompany = false;

    public void useCompany() {
        useCompany = true;
    }

    private boolean useDuty = false;

    public void useDuty() {
        useDuty = true;
    }

    private boolean useHomepage = false;

    public void useHomepage() {
        useHomepage = true;
    }

    private boolean useBirthday = false;

    public void useBirthday() {
        useBirthday = true;
    }

    private boolean useGroup = false;

    public void useGroup() {
        useGroup = true;
    }

    private boolean flatNumber = false;

    public void flatNumber() {
        flatNumber = true;
    }

    public void useAll() {
        useEmail = useAddr = useMemo = useCompany = useDuty = useHomepage = useBirthday = useGroup = true;
    }

    public PhonebookReader(Context ctx) {
        this.ctx = ctx;
    }

    public List<Member> getMemberList() {
        List<Member> list = new ArrayList<Member>();
        Map<Integer, Member> map = new HashMap<Integer, Member>();
        Member node = null;
        int lastId = -1;
        Map<Long, String> groupList = useGroup ? getGroupList() : null;

        Cursor c = ctx.getContentResolver().query
                (
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, Data.CONTACT_ID
                );

        // 아이디
        int c_id = c.getColumnIndex(Data.CONTACT_ID);
        int c_type = c.getColumnIndex(Phone.TYPE);
        final int c_name = c.getColumnIndex(Contacts.DISPLAY_NAME);
        final int p_number = c.getColumnIndex(Phone.NUMBER);

        while (c.moveToNext()) {
            // 아이디
            int id = c.getInt(c_id);
            int type = c.getInt(c_type);

            // 신규작성
            if (id != lastId) {
                lastId = id;
                list.add((node = new Member()));
                map.put(id, node);
                node.id = id;
                node.name = c.getString(c_name);
            }

            String number = filtLinePhone(c.getString(p_number));

            if (number != null) {
                if (isMobilePhone(number)) {
                    if (node.mobilePhone == null) {
                        if (flatNumber) number = flatNumber(number);
                        node.mobilePhone = number;
                    }
                } else {
                    if (flatNumber) number = flatNumber(number);
                    switch (type) {
                        // 집전화
                        case Phone.TYPE_HOME:
                            node.homePhone = number;
                            break;
                        // 회사전화
                        case Phone.TYPE_COMPANY_MAIN:
                        case Phone.TYPE_WORK:
                            node.officePhone = number;
                            break;
                        // 팩스
                        case Phone.TYPE_FAX_HOME:
                        case Phone.TYPE_FAX_WORK:
                        case Phone.TYPE_OTHER_FAX:
                            node.fax = number;
                            break;
                    }
                }
            }
        }

        c.close();

        if (needMobPhone) {
            Map<String, Character> dup = new HashMap<String, Character>();
            String hp;

            for (int i = 0; i < list.size(); ) {
                hp = list.get(i).mobilePhone;
                if (hp != null && dup.get(hp) == null) {
                    dup.put(hp, '1');
                    i++;
                } else {
                    list.remove(i);
                }
            }
        }

        c = ctx.getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, null, null, Data.CONTACT_ID);
        c_id = c.getColumnIndex(Data.CONTACT_ID);
        c_type = c.getColumnIndex(StructuredPostal.TYPE);
        int c_mime = c.getColumnIndex(Data.MIMETYPE);

        int c_mail = c.getColumnIndex(Email.DATA);
        int c_memo = c.getColumnIndex(Note.NOTE);
        int c_com = c.getColumnIndex(Organization.COMPANY);
        int c_duty = c.getColumnIndex(Organization.TITLE);
        int c_site = c.getColumnIndex(Website.URL);
        int c_bd = c.getColumnIndex(Event.START_DATE);
        int c_haddr = c.getColumnIndex(StructuredPostal.FORMATTED_ADDRESS);
        int c_hazip = c.getColumnIndex(StructuredPostal.POBOX);
        int c_oaddr = c.getColumnIndex(StructuredPostal.FORMATTED_ADDRESS);
        int c_oazip = c.getColumnIndex(StructuredPostal.POBOX);
        int c_groupno = c.getColumnIndex(GroupMembership.GROUP_ROW_ID);
        lastId = -1; // 초기화

        String val;

        while (c.moveToNext()) {
            int id = c.getInt(c_id);

            // 아이디가 달라지면
            if (id != lastId) {
                lastId = id;
                node = map.get(c.getInt(c_id));
            }
            // 없는정보
            if (node == null) {
                continue;
            }

            // 마임
            String mime = c.getString(c_mime);

            if (mime.equals(Email.CONTENT_ITEM_TYPE)) {
                if (useEmail && isEmail(val = c.getString(c_mail))) {
                    node.email = val;
                }
            } else if (mime.equals(Note.CONTENT_ITEM_TYPE)) {
                if (useMemo && (val = c.getString(c_memo)) != null) {
                    node.memo = val;
                }
            } else if (mime.equals(Organization.CONTENT_ITEM_TYPE)) {
                if (useCompany && (val = c.getString(c_com)) != null) {
                    node.company = val;
                }

                if (useDuty && (val = c.getString(c_duty)) != null) {
                    node.duty = val;
                }
            } else if (mime.equals(Website.CONTENT_ITEM_TYPE)) {
                if (useHomepage && c.getType(c_type) == Website.TYPE_HOMEPAGE && (val = c.getString(c_site)) != null) {
                    node.homepage = val;
                }
            } else if (mime.equals(Event.CONTENT_ITEM_TYPE)) {
                if (useBirthday && c.getType(c_type) == Event.TYPE_BIRTHDAY && (val = c.getString(c_bd)) != null) {
                    node.birthday = val;
                }
            } else if (mime.equals(StructuredPostal.CONTENT_ITEM_TYPE)) {
                if (useAddr) {
                    switch (c.getType(c_type)) {
                        case StructuredPostal.TYPE_HOME:
                            if ((val = c.getString(c_haddr)) != null) {
                                node.homeAddr = val;
                            }
                            if ((val = c.getString(c_hazip)) != null) {
                                node.homeZip = val;
                            }
                            break;
                        case StructuredPostal.TYPE_WORK:
                            if ((val = c.getString(c_oaddr)) != null) {
                                node.officeAddr = val;
                            }
                            if ((val = c.getString(c_oazip)) != null) {
                                node.officeZip = val;
                            }
                            break;
                    }
                }
            } else if (mime.equals(GroupMembership.CONTENT_ITEM_TYPE)) {
                // 그룹이 발견될 경우 더 이상 찾지 않기 위해.
                if (useGroup && node.groupNo == 0) {
                    node.groupName = groupList.get((node.groupNo = c.getLong(c_groupno)));
                }
            }
        }

        c.close();

        return list;
    }

    /**
     * 그룹을 가져온다.
     */
    public Map<Long, String> getGroupList() {
        Map<Long, String> groupList = new HashMap<Long, String>();

        Cursor c = ctx.getContentResolver().query
                (
                        Groups.CONTENT_SUMMARY_URI, new String[]{Groups._ID, Groups.TITLE}, null, null, null
                );

        int c_id = c.getColumnIndex(Groups._ID);
        int c_name = c.getColumnIndex(Groups.TITLE);

        while (c.moveToNext()) {
            groupList.put(c.getLong(c_id), c.getString(c_name));
        }

        c.close();

        return groupList;
    }

    final private static Pattern PAT_COUNTRY_CODE_KOREA = Pattern.compile("^(\\+|\\-)?82\\-?");
    final private static Pattern PAT_PHONE_NOP = Pattern.compile("^[\\d]{9,11}$");
    final private static Pattern PAT_PHONE = Pattern.compile("^[\\d]{2,3}\\-[\\d]{3,4}\\-[\\d]{4}$");

    private String filtLinePhone(String no) {
        if (no == null || no.length() == 0) {
            return null;
        }

        // 국가번호를 제거하고 정규화함.
        if (!(no = PAT_COUNTRY_CODE_KOREA.matcher(no).replaceFirst("")).startsWith("0")) {
            no = '0' + no;
        }

        // 하이픈이 없는 경우 [정규화]
        if (no.indexOf('-') == -1) {
            switch (no.length()) {
                case 9:
                    no = no.substring(0, 2) + '-' + no.substring(2, 5) + '-' + no.substring(5);
                    break;
                case 10:
                    no = no.startsWith("02")
                            ? (no.substring(0, 2) + '-' + no.substring(2, 6) + '-' + no.substring(6))
                            : (no.substring(0, 3) + '-' + no.substring(3, 6) + '-' + no.substring(6));
                    break;
                case 11:
                    no = no.substring(0, 3) + '-' + no.substring(3, 7) + '-' + no.substring(7);
                    break;
                default:
                    return null;
            }
            return no;
        }

        if (PAT_PHONE.matcher(no).matches() && no.length() >= 11 && no.length() <= 13) {
            return no;
        }

        return no;
    }

    private String flatNumber(String no) {
        if (no == null || no.length() == 0) {
            return null;
        }
        if (!(no = PAT_COUNTRY_CODE_KOREA.matcher(no).replaceFirst("")).startsWith("0")) {
            no = '0' + no;
        }
        return no.replaceAll("\\D", "");
    }

    final private static Pattern PAT_MOBILE_PHONE = Pattern.compile("^01(0|1|6|7|8|9)\\-[\\d\\-]+");

    /**
     * 주의 먼저 filtLinePhone에 통과된것을 사용하자
     */
    public static boolean isMobilePhone(String no) {
        return PAT_MOBILE_PHONE.matcher(no).matches();
    }

    final public static Pattern PAT_MAIL = Pattern.compile("^[\\._a-z0-9\\-]+@[\\._a-z0-9\\-]+\\.[a-z]{2,}$");

    private boolean isEmail(String email) {
        return email != null && PAT_MAIL.matcher(email).matches();
    }

    public static class Member implements Comparable<Member> {
        public int id;
        public String name;
        public String mobilePhone;
        public String homePhone;
        public String officePhone;
        public String fax;
        public String homeAddr;
        public String homeZip;
        public String officeAddr;
        public String officeZip;
        public String email;
        public String memo;
        public String company;
        public String duty;
        public String homepage;
        public String birthday;
        public long groupNo = 0;
        public String groupName;

        public void log(String tag) {
            // 로그용인데 스트링빌더 필요없겠지...
            String text = "[" + id + "] " + name;
            Log.d(tag, text);
            text = "- " + mobilePhone + ", " + homePhone + ", " + officePhone + ", " + fax + ", " +
                    homeAddr + ", " + homeZip + ", " + officeAddr + ", " + officeZip + ", " +
                    email + ", " + memo + ", " + company + ", " + duty + ", " + homepage + ", " + birthday + ", " + groupName;
            Log.d(tag, text);
        }

        public String toString() {
            return "[" + id + "] " + name + ":" + mobilePhone + ", " + homePhone + ", " + officePhone + ", " + fax + ", " +
                    homeAddr + ", " + homeZip + ", " + officeAddr + ", " + officeZip + ", " +
                    email + ", " + memo + ", " + company + ", " + duty + ", " + homepage + ", " + birthday + ", " + groupName;

        }

        @Override
        public int compareTo(Member member) {
            if (name == null || member.name == null)
                return 0;
            return name.compareTo(member.name);
        }
    }

//    public static void getAddr(Context context){
//        Cursor cursor = null;
//        try {
//                    Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
//            String phoneName = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME;
//            String [] ad = new String[] {
//                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
//                    ContactsContract.CommonDataKinds.Phone.NUMBER
//            };
//            cursor = context.getContentResolver().query(uri, ad, null, null, phoneName);
//            cursor.moveToFirst();
//            while(cursor.moveToNext())
//            {
//                if(cursor.getString(1) !=null)
//                {
//                    if(list.size()==0)
//                    {
//                        bean = new AddrBean();
//                        bean.setName(cursor.getString(0));
//                        bean.setNumber(cursor.getString(1));
//                        list.add(bean);
//                    }
//                    else{
//                        if(!list.get(list.size()-1).getNumber().equals(cursor.getString(1)))
//                        {
//                            bean = new AddrBean();
//                            bean.setName(cursor.getString(0));
//                            bean.setNumber(cursor.getString(1));
//                            list.add(bean);
//                        }
//                    }
//                }
//            }
//            deptList = list;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }finally{
//            if(cursor != null){
//                cursor.close();
//                cursor = null;
//            }
//        }
//
//    }
}