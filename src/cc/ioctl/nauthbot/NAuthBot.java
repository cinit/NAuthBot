package cc.ioctl.nauthbot;


import nil.test.uncertaintycalc.junk.RandomExprGenerator;
import nil.test.uncertaintycalc.math.RuntimeEnv;
import nil.test.uncertaintycalc.math.expr.Expression;
import nil.test.uncertaintycalc.math.expr.ExpressionFactory;
import nil.test.uncertaintycalc.math.expr.NumericVariable;
import nil.test.uncertaintycalc.math.func.Cos;
import nil.test.uncertaintycalc.math.func.Ln;
import nil.test.uncertaintycalc.math.func.Sin;
import nil.test.uncertaintycalc.math.func.Sqrt;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class NAuthBot extends TelegramLongPollingBot {
    ExecutorService tp = Executors.newCachedThreadPool();

    private final AtomicInteger nextAuthId = new AtomicInteger((int) (100000 + Math.random() * 66666666));
    private final ConcurrentHashMap<Integer, AuthProgressInfo> authList = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Integer> groupPolicy = new ConcurrentHashMap<>();

    public static final long sBotStartupTime = System.currentTimeMillis() / 1000L;
    public boolean beforeSync = true;
    public int skipMsgCount = 0;
    private static final BigDecimal AUTH_PRECISION = new BigDecimal("0.00001");

    public NAuthBot(DefaultBotOptions opt) {
        super(opt);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!beforeSync) {
            System.out.print(update.getUpdateId() + "->");
        }
        if (update.hasMessage()) {
            final Message msg = update.getMessage();
            if (beforeSync) {
                if (msg.getDate() < sBotStartupTime) {
                    skipMsgCount++;
                    return;
                } else {
                    beforeSync = false;
                    System.out.println("Sync msg done, dropped " + skipMsgCount + " msg.");
                }
            }
            final Chat chat = msg.getChat();
            List<User> newGuys = msg.getNewChatMembers();
            if (newGuys.size() != 0 && (chat.isGroupChat() || chat.isSuperGroupChat())) {
                int policy = getGroupPolicy(chat.getId());
                if (policy > 1) {
                    for (User u : newGuys) {
                        asyncStartWelcome(chat, u, msg.getMessageId(), policy == 2);
                    }
                }
            } else {
                AuthProgressInfo __info = findAuthInfoByCUID(chat, msg.getFrom());
                if (__info != null) {
                    String text = msg.getText();
                    if (text == null) text = "";
                    checkInput(__info, text);
                    __info.inMsgUid = msg.getMessageId();
                    final SendMessage respMsg = new SendMessage();
                    respMsg.setChatId(chat.getId());
                    respMsg.setReplyToMessageId(msg.getMessageId());
                    respMsg.enableMarkdownV2(true);
                    respMsg.setText(getCurrentDisplayText(__info));
                    if (__info.status != AuthProgressInfo.Status.PASSED) {
                        respMsg.setReplyMarkup(getEntranceButtons(__info.cid, __info.uid, __info.id));
                    }
                    asyncReplaceMsg(respMsg, __info, true);
                    if (__info.status == AuthProgressInfo.Status.PASSED) {
                        doMemberAuthPass(__info);
                    }
                } else if (chat.isUserChat()) {
                    String reply = msg.getChatId() + ": " + msg.getText();
                    System.out.println(reply);
                    String text = msg.getText();
                    if (text != null && text.startsWith("/test_math")) {
                        asyncStartWelcome(chat, msg.getFrom(), msg.getMessageId(), true);
                    } else {
                        final SendMessage respMsg = new SendMessage();
                        respMsg.setChatId(chat.getId());
                        respMsg.setText(reply);
                        respMsg.enableMarkdown(false);
                        asyncExecute(respMsg);
                    }
                } else if (chat.isGroupChat() || chat.isSuperGroupChat()) {
                    String logMsg = msg.getFrom().getFirstName() + "(" + msg.getFrom().getId() + ")@" + msg.getChatId() + ": " + msg.getText();
                    System.out.println(logMsg);
                    String text = msg.getText();
                    if (text != null && text.startsWith("/configure")) {
                        int policyNew = getPolicyFromString(text);
                        if (policyNew == -1) {
                            final SendMessage respMsg = new SendMessage();
                            respMsg.setChatId(chat.getId());
                            respMsg.setReplyToMessageId(msg.getMessageId());
                            respMsg.setText("Usage: /configure \\(`silent`\\|`passive`\\|`demo`\\|`full`\\)\n" +
                                    "Current policy for this group: `" + policyToString(getGroupPolicy(chat.getId())) + "`");
                            respMsg.enableMarkdownV2(true);
                            asyncExecute(respMsg);
                        } else {
                            GetChatAdministrators ga = new GetChatAdministrators();
                            ga.setChatId(chat.getId());
                            asyncExecute(ga, new Consumer<ArrayList<ChatMember>>() {
                                @Override
                                public void accept(ArrayList<ChatMember> chatMembers) {
                                    final SendMessage respMsg = new SendMessage();
                                    boolean isAdmin = false;
                                    Integer uid = msg.getFrom().getId();
                                    for (ChatMember admin : chatMembers) {
                                        if (uid.equals(admin.getUser().getId())) {
                                            isAdmin = true;
                                            break;
                                        }
                                    }
                                    if (isAdmin) {
                                        groupPolicy.put(chat.getId(), policyNew);
                                        respMsg.setChatId(chat.getId());
                                        respMsg.setReplyToMessageId(msg.getMessageId());
                                        respMsg.setText("Success: `" + policyToString(policyNew) + "`");
                                        respMsg.enableMarkdownV2(true);
                                        asyncExecute(respMsg);
                                    } else {
                                        respMsg.setChatId(chat.getId());
                                        respMsg.setReplyToMessageId(msg.getMessageId());
                                        respMsg.setText("Only admins can execute this cmd.");
                                        respMsg.enableMarkdownV2(false);
                                        asyncExecute(respMsg);
                                    }
                                }
                            });
                        }
                    } else if (text != null && text.startsWith("/test_math")) {
                        asyncStartWelcome(chat, msg.getFrom(), msg.getMessageId(), true);
                    }
                } else {
                    System.out.println(msg.getFrom().getId() + "@channel[" + chat.getId() + "]" + msg.getMessageId());
                }
            }
        }
        if (update.hasCallbackQuery()) {
            CallbackQuery query = update.getCallbackQuery();
            String data = query.getData();
            String[] argList;
            boolean error = false;
            int authId = -1;
            int which = -1;
            if (data == null || (argList = data.split(",")).length != 2) {
                error = true;
            } else {
                try {
                    which = Integer.parseInt(argList[0]);
                    authId = Integer.parseInt(argList[1]);
                } catch (NumberFormatException e) {
                    error = true;
                }
            }
            if (error) {
                AnswerCallbackQuery ans = new AnswerCallbackQuery();
                ans.setCallbackQueryId(query.getId());
                ans.setText("503 Internal Service Error");
                ans.setShowAlert(true);
                asyncExecute(ans);
            } else {
                AuthProgressInfo info = authList.get(authId);
                if (info == null) {
                    AnswerCallbackQuery ans = new AnswerCallbackQuery();
                    ans.setCallbackQueryId(query.getId());
                    ans.setText("出了点问题, 消息太久远找不到啦");
                    ans.setShowAlert(true);
                    asyncExecute(ans);
                } else {
                    switch (which) {
                        case 0: {
                            if (!info.uid.getId().equals(query.getFrom().getId())) {
                                AnswerCallbackQuery ans = new AnswerCallbackQuery();
                                ans.setCallbackQueryId(query.getId());
                                ans.setText("与汝无瓜");
                                ans.setShowAlert(true);
                                asyncExecute(ans);
                            } else {
                                info.expr = generateMathExercise();
                                AnswerCallbackQuery ans = new AnswerCallbackQuery();
                                ans.setCallbackQueryId(query.getId());
                                ans.setText("正在重新加载");
                                ans.setShowAlert(false);
                                asyncExecute(ans);
                                EditMessageText edit = new EditMessageText();
                                edit.setParseMode(ParseMode.MARKDOWNV2);
                                edit.setChatId(info.cid.getId());
                                edit.setMessageId(info.outMsgUid);
                                edit.setText(getCurrentDisplayText(info));
                                edit.setReplyMarkup(getEntranceButtons(info.cid, info.uid, info.id));
                                asyncExecute(edit);
                            }
                            break;
                        }
                        case 1:
                        case 3:
                        case 2: {
                            AnswerCallbackQuery ans = new AnswerCallbackQuery();
                            if (info.uid.getId().equals(query.getFrom().getId())) {
                                ans.setCallbackQueryId(query.getId());
                                ans.setText("禁止自娱自乐");
                                ans.setShowAlert(true);
                                asyncExecute(ans);
                            } else {
                                if (which == 2) {
                                    ans.setCallbackQueryId(query.getId());
                                    ans.setText("需要管理员权限");
                                    ans.setShowAlert(true);
                                    asyncExecute(ans);
                                } else if (which == 1) {
                                    info.status = AuthProgressInfo.Status.MANUAL_ACCEPT;
                                    EditMessageText edit = new EditMessageText();
                                    edit.setParseMode(ParseMode.MARKDOWNV2);
                                    edit.setChatId(info.cid.getId());
                                    edit.setMessageId(info.outMsgUid);
                                    edit.setText(getCurrentDisplayText(info));
                                    asyncExecute(edit);
                                    doMemberAuthPass(info);
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    void asyncStartWelcome(Chat cid, User uid, int msgUid, boolean test) {
        final int authId = nextAuthId.incrementAndGet();
        final AuthProgressInfo info = new AuthProgressInfo();
        info.cid = cid;
        info.id = authId;
        info.time = System.currentTimeMillis();
        info.test = test;
        info.uid = uid;
        info.status = AuthProgressInfo.Status.NOT_RESPONDED;
        info.inMsgUid = msgUid;
        authList.put(authId, info);
        SendMessage respMsg = new SendMessage();
        respMsg.setChatId(cid.getId());
        respMsg.setReplyToMessageId(msgUid);
        respMsg.enableMarkdownV2(true);
//        respMsg.setReplyMarkup()
        respMsg.setReplyMarkup(getEntranceButtons(cid, uid, authId));
        info.expr = generateMathExercise();
        respMsg.setText(getCurrentDisplayText(info));
        asyncExecute(respMsg, new Consumer<Message>() {
            @Override
            public void accept(Message message) {
                try {
                    int errCount = 0;
                    info.outMsgUid = message.getMessageId();
                    Thread.sleep(10_000);
                    while ((System.currentTimeMillis() - info.time < 270_000)
                            && (info.status == AuthProgressInfo.Status.NOT_RESPONDED ||
                            info.status == AuthProgressInfo.Status.INCORRECT)) {
                        EditMessageText edit = new EditMessageText();
                        edit.setParseMode(ParseMode.MARKDOWNV2);
                        edit.setChatId(info.cid.getId());
                        edit.setMessageId(info.outMsgUid);
                        edit.setText(getCurrentDisplayText(info));
                        edit.setReplyMarkup(getEntranceButtons(info.cid, info.uid, info.id));
                        try {
                            execute(edit);
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (errCount++ > 10) throw e;
                        }
                        Thread.sleep(30_000);
                    }
                    if (info.status == AuthProgressInfo.Status.NOT_RESPONDED ||
                            info.status == AuthProgressInfo.Status.INCORRECT) {
                        info.status = AuthProgressInfo.Status.TIMEOUT;
                        EditMessageText edit = new EditMessageText();
                        edit.setParseMode(ParseMode.MARKDOWNV2);
                        edit.setChatId(info.cid.getId());
                        edit.setMessageId(info.outMsgUid);
                        edit.setText(getCurrentDisplayText(info));
                        execute(edit);
                        doMemberAuthFailed(info);
                    }
                    authList.remove(authId);
                    Thread.sleep(90_000);
                    DeleteMessage delMsg = new DeleteMessage();
                    delMsg.setChatId(message.getChatId());
                    delMsg.setMessageId(info.outMsgUid);
                    execute(delMsg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    String getCurrentDisplayText(final AuthProgressInfo info) {
        User uid = info.uid;
        if (info.status == AuthProgressInfo.Status.NOT_RESPONDED) {
            long timeLeft = 271 - (System.currentTimeMillis() - info.time) / 1000L;
            timeLeft = Math.max(0, timeLeft);
            timeLeft = Math.min(270, timeLeft);
            return "欢迎 [" + wrapSpecChars(uid.getFirstName() + ((isEmpty(info.uid.getLastName())) ? "" : " " + info.uid.getLastName()))
                    + "](tg://user?id=" + uid.getId() + ") 加入本群, 本群已开启验证, " +
                    "请在 *__" + timeLeft + "__* 秒内完成对以下表达式 __*求导*__\n" +
                    "`" + info.expr.toString() + "`\n并直接在群组中发送, 超时或验证失败将会被移除本群\\(2min后可重新加入\\)";
        } else if (info.status == AuthProgressInfo.Status.INCORRECT) {
            long timeLeft = 271 - (System.currentTimeMillis() - info.time) / 1000L;
            timeLeft = Math.max(0, timeLeft);
            timeLeft = Math.min(270, timeLeft);
            String result = "这是 [" + wrapSpecChars(uid.getFirstName() + ((isEmpty(info.uid.getLastName())) ? "" : " " + info.uid.getLastName()))
                    + "](tg://user?id=" + uid.getId() + ") 第 __*" + info.errors + "*__ 次回答错误, 请继续尝试对\n" +
                    "`" + info.expr.toString() + "`\n求导, 剩余时间: *__" + timeLeft + "__* 秒";
            if (info.lastErrorMsg != null) {
                result += ", " + wrapSpecChars(info.lastErrorMsg);
            }
            return result;
        } else if (info.status == AuthProgressInfo.Status.MANUAL_ACCEPT) {
            return "[" + wrapSpecChars(uid.getFirstName() + ((isEmpty(info.uid.getLastName())) ? "" : " " + info.uid.getLastName()))
                    + "](tg://user?id=" + uid.getId() + ") 被人工通过了";
        } else if (info.status == AuthProgressInfo.Status.MANUAL_DENY) {
            return "[" + wrapSpecChars(uid.getFirstName() + ((isEmpty(info.uid.getLastName())) ? "" : " " + info.uid.getLastName()))
                    + "](tg://user?id=" + uid.getId() + ") 被人工拒绝了";
        } else if (info.status == AuthProgressInfo.Status.PASSED) {
            long cost = (System.currentTimeMillis() - info.time) / 1000L;
            return "[" + wrapSpecChars(uid.getFirstName() + ((isEmpty(info.uid.getLastName())) ? "" : " " + info.uid.getLastName()))
                    + "](tg://user?id=" + uid.getId() + ") 通过了验证, 用时 __*" + cost + "*__ 秒";
        } else if (info.status == AuthProgressInfo.Status.TIMEOUT) {
            return "[" + wrapSpecChars(uid.getFirstName() + ((isEmpty(info.uid.getLastName())) ? "" : " " + info.uid.getLastName()))
                    + "](tg://user?id=" + uid.getId() + ") 超时验证失败, \\(假装\\)被移除本群\\(还没写\\), 两分钟后可以重新申请加群";
        } else {
            return "[" + wrapSpecChars(uid.getFirstName() + ((isEmpty(info.uid.getLastName())) ? "" : " " + info.uid.getLastName()))
                    + "](tg://user?id=" + uid.getId() + ") 503 Internal Service Error";
        }
    }

    public void doMemberAuthPass(AuthProgressInfo info) {
        authList.remove(info.id);

    }

    public void doMemberAuthFailed(AuthProgressInfo info) {
        authList.remove(info.id);
    }

    public static String wrapSpecChars(String in) {
        String __in = in;
        char[] badChars = new char[]{'_', '*', '[', ']', '(', ')', '~', '`', '>', '#', '+', '-', '=', '|', '{', '}', '.', '!'};
        in = in.replace("\\", "\\\\");
        for (char c : badChars) {
            in = in.replace("" + c, "\\" + c);
        }
        //System.out.printf("'%s'->'%s'\n", __in, in);
        return in;
    }

    void asyncReplaceMsg(final SendMessage action, final AuthProgressInfo info, boolean delOrig) {
        tp.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Message outNew = execute(action);
                    int outOld = info.outMsgUid;
                    info.outMsgUid = outNew.getMessageId();
                    DeleteMessage delMsg = new DeleteMessage();
                    delMsg.setChatId(info.cid.getId());
                    delMsg.setMessageId(outOld);
                    execute(delMsg);
                    if (delOrig) {
                        delMsg = new DeleteMessage();
                        delMsg.setChatId(info.cid.getId());
                        delMsg.setMessageId(info.inMsgUid);
                        execute(delMsg);
                    }
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    AuthProgressInfo findAuthInfoByCUID(Chat cid, User uid) {
        for (AuthProgressInfo info : authList.values()) {
            if (info.uid.getId().equals(uid.getId()) && cid.getId().equals(info.cid.getId())) {
                return info;
            }
        }
        return null;
    }

    private <T extends Serializable, Method extends BotApiMethod<T>> void asyncExecute(Method method) {
        asyncExecute(method, null);
    }

    private Expression generateMathExercise() {
        return RandomExprGenerator.next((int) (Math.random() * 100f + 30f));
    }

    private <T extends Serializable, Method extends BotApiMethod<T>> void asyncExecute(Method method, Consumer<T> callback) {
        tp.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    T result = execute(method);
                    if (callback != null) {
                        callback.accept(result);
                    }
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    void checkInput(AuthProgressInfo info, String input) {
        try {
            input = input.replace(" ", "").replace("**", "^");
            RuntimeEnv rt = new RuntimeEnv(MathContext.DECIMAL32);
            Map<String, Object> vars = new HashMap<String, Object>();
            vars.put("sqrt", new Sqrt());
            vars.put("sin", new Sin());
            vars.put("cos", new Cos());
            vars.put("ln", new Ln());
            vars.put("log", new Ln());
            vars.put("x", new NumericVariable("x"));
            Expression expr1 = ExpressionFactory.createFromString(input, vars, false);
            Expression expr2 = info.expr.differentiate("x", vars);
            double d1;

            boolean correct = false;
            Throwable err = null;
            try {
                d1 = Math.random();
                vars.put("x", BigDecimal.valueOf(d1));
                err = null;
                correct = correct || (expr1.eval(rt, vars).add(expr2.eval(rt, vars).negate()).abs().compareTo(AUTH_PRECISION) <= 0);
            } catch (Throwable e) {
                err = e;
            }
            try {
                d1 = -Math.random();
                vars.put("x", BigDecimal.valueOf(d1));
                err = null;
                correct = correct || (expr1.eval(rt, vars).add(expr2.eval(rt, vars).negate()).abs().compareTo(AUTH_PRECISION) <= 0);
            } catch (Throwable e) {
                err = e;
            }
            try {
                d1 = 9 + Math.random();
                vars.put("x", BigDecimal.valueOf(d1));
                err = null;
                correct = correct || (expr1.eval(rt, vars).add(expr2.eval(rt, vars).negate()).abs().compareTo(AUTH_PRECISION) <= 0);
            } catch (Throwable e) {
                err = e;
            }
            try {
                d1 = -10 + Math.random();
                vars.put("x", BigDecimal.valueOf(d1));
                err = null;
                correct = correct || (expr1.eval(rt, vars).add(expr2.eval(rt, vars).negate()).abs().compareTo(AUTH_PRECISION) <= 0)
                ;
            } catch (Throwable e) {
                err = e;
            }
            if (!correct) {
                info.status = AuthProgressInfo.Status.INCORRECT;
                info.lastErrorMsg = "表达式结果不正确";
                info.errors++;
            } else {
                info.status = AuthProgressInfo.Status.PASSED;
                info.lastErrorMsg = null;
            }
        } catch (Throwable e) {
            info.status = AuthProgressInfo.Status.INCORRECT;
            info.lastErrorMsg = e.getMessage();
            info.errors++;
        }
    }

    @Override
    public String getBotUsername() {
        return PrivateKeys.USERNAME;
    }

    @Override
    public String getBotToken() {
        return PrivateKeys.API_KEY;
    }

    private static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    InlineKeyboardMarkup getEntranceButtons(Chat cid, User uid, int authId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        ArrayList<InlineKeyboardButton> l1 = new ArrayList<>();
        l1.add(new InlineKeyboardButton().setText("换一题").setCallbackData("0," + authId));
        ArrayList<InlineKeyboardButton> l2 = new ArrayList<>();
        l2.add(new InlineKeyboardButton().setText("人工通过").setCallbackData("1," + authId));
        l2.add(new InlineKeyboardButton().setText("人工拒绝").setCallbackData("2," + authId));
        List<List<InlineKeyboardButton>> btns = new ArrayList<>();
        btns.add(l1);
        btns.add(l2);
        markup.setKeyboard(btns);
        return markup;
    }

    int getGroupPolicy(long id) {
        Integer p = groupPolicy.get(id);
        if (p == null) return 1;
        else return p;
    }

    int getPolicyFromString(String str) {
        if (str == null) return -1;
        str = str.toLowerCase();
        if (str.contains("silent")) return 0;
        if (str.contains("passive")) return 1;
        if (str.contains("demo")) return 2;
        if (str.contains("full")) return 3;
        return -1;
    }

    String policyToString(int i) {
        switch (i) {
            case 0:
                return "silent";
            case 1:
                return "passive";
            case 2:
                return "demo";
            case 3:
                return "full";
            default:
                return "unknown";
        }
    }

    public static class AuthProgressInfo {
        public int id;
        public long time;
        public Chat cid;
        public User uid;
        public int errors;
        public String lastErrorMsg;
        public Status status = Status.NOT_RESPONDED;
        public Expression expr;
        public int inMsgUid;
        public int outMsgUid;
        public boolean test;

        public static enum Status {
            NOT_RESPONDED,
            TIMEOUT,
            INCORRECT,
            PASSED,
            MANUAL_ACCEPT,
            MANUAL_DENY
        }
    }
}
