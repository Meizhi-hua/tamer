public final class ServiceDetail implements org.omg.CORBA.portable.IDLEntity
{
    public int service_detail_type;
    public byte[] service_detail;
    public ServiceDetail() { }
    public ServiceDetail(int service_detail_type, byte[] service_detail) {
        this.service_detail_type = service_detail_type;
        this.service_detail = service_detail;
    }
}
