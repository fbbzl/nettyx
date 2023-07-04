import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.annotation.FieldHandler;
import org.fz.nettyx.serializer.annotation.Length;
import org.fz.nettyx.serializer.annotation.Struct;
import org.fz.nettyx.serializer.handler.ReadWriteHandler;
import org.fz.nettyx.serializer.serializer.type.TypedByteBufSerializer;
import types.*;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2021/10/20 16:27
 */
public class DemoMain {

    public static void main(String[] args) {
        // these bytes may from nio, netty, inputstream, outputstream.....
        byte[] bytes = {12, -11, -45, -123, -67, -57, -90, -99, -11, -22, -78, -90, -45, -33, -67, -56, -67, -77, -7, -55, -66, -45, -77, -77, -45, -55, -62,
            -90, -45, -3, -111, -77, -55, -52, -56, -77, -45, -54, -45, -52, -23, -44, -35, -11, -78, -51, -112, -42, -22, -45, -33, -52, -45, -99, -33, -11, -66,
            -6, -78, -1, 48, 111, 12};

        User demo = TypedByteBufSerializer.read(Unpooled.wrappedBuffer(bytes), User.class);
        System.err.println("Read data: " + demo);

        // test write
        Bill bill = new Bill();
        bill.setIsSuccess(cboolean.TRUE);
        bill.setBid("9527");

        final User user = new User();
        user.setUid(clong.of(2));
        user.setUname(cchar.of(1));
        user.setIsMarried(cboolean.TRUE);
        user.setSex(cbyte.of(1));
        user.setAddress(cdword.of(1L));
        user.setPlatformId(cshort.of(1));
        user.setDescription(cword.of(123));
        user.setBill(bill);
        user.setLoginNames(new cdword[]{cdword.of(122L)});

        final byte[] userWriteBytes = TypedByteBufSerializer.writeBytes(user);

        System.err.println("Write data: " + Arrays.toString(userWriteBytes));
    }

    public static class InnerEntityHandler implements ReadWriteHandler {

        @Override
        public String doRead(TypedByteBufSerializer serializer, Field field) {
            return "■■■■■■this value may from DB or the other way■■■■■";
        }

        @Override
        public void doWrite(TypedByteBufSerializer serializer, Field field, Object value, ByteBuf add) {
            add.writeBytes(new byte[]{99, 99, 99, 99, 99});
        }
    }

    @Struct
    public static class Bill {

        @FieldHandler(InnerEntityHandler.class)
        private String bid;
        private cboolean isSuccess;

        @Override
        public String toString() {
            return "Bill{" +
                "boolean_=" + isSuccess +
                ", bid=" + bid +
                '}';
        }

        public cboolean getIsSuccess() {
            return isSuccess;
        }

        public void setIsSuccess(cboolean isSuccess) {
            this.isSuccess = isSuccess;
        }

        public String getBid() {
            return bid;
        }

        public void setBid(String bid) {
            this.bid = bid;
        }
    }

    @Struct
    public static class User {

        private clong uid;
        private cchar uname;
        private cboolean isMarried;
        private cbyte sex;
        private cdword address;
        private cshort platformId;
        private cword description;
        private Bill bill;
        @Length(2)
        private cdword[] loginNames;

        public clong getUid() {
            return uid;
        }

        public void setUid(clong uid) {
            this.uid = uid;
        }

        public cchar getUname() {
            return uname;
        }

        public void setUname(cchar uname) {
            this.uname = uname;
        }

        public cboolean getIsMarried() {
            return isMarried;
        }

        public void setIsMarried(cboolean isMarried) {
            this.isMarried = isMarried;
        }

        public cbyte getSex() {
            return sex;
        }

        public void setSex(cbyte sex) {
            this.sex = sex;
        }

        public cdword getAddress() {
            return address;
        }

        public void setAddress(cdword address) {
            this.address = address;
        }

        public cshort getPlatformId() {
            return platformId;
        }

        public void setPlatformId(cshort platformId) {
            this.platformId = platformId;
        }

        public cword getDescription() {
            return description;
        }

        public void setDescription(cword description) {
            this.description = description;
        }

        public Bill getBill() {
            return bill;
        }

        public void setBill(Bill bill) {
            this.bill = bill;
        }

        public cdword[] getLoginNames() {
            return loginNames;
        }

        public void setLoginNames(cdword[] loginNames) {
            this.loginNames = loginNames;
        }

        @Override
        public String toString() {
            return "User{" +
                "uid=" + uid +
                ", uname=" + uname +
                ", isMarried=" + isMarried +
                ", sex=" + sex +
                ", address=" + address +
                ", platformId=" + platformId +
                ", description=" + description +
                ", bill=" + bill +
                ", loginNames=" + Arrays.toString(loginNames) +
                '}';
        }
    }
}
