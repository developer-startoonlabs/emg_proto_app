package com.start.apps.pheezee.pojos;

public class ConfirmEmailAndPackageId {
    String phizioemail, otp;

    public ConfirmEmailAndPackageId(String phizioemail, String otp) {
        this.phizioemail = phizioemail;
        this.otp = otp;
    }

    public String getPhizioemail() {
        return phizioemail;
    }

    public void setPhizioemail(String phizioemail) {
        this.phizioemail = phizioemail;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}
