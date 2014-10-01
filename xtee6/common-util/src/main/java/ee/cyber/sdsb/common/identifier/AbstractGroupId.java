package ee.cyber.sdsb.common.identifier;

public abstract class AbstractGroupId extends SdsbId {

    private final String groupCode;

    AbstractGroupId() { // required by Hibernate
        this(null, null, null);
    }

    protected AbstractGroupId(SdsbObjectType type, String sdsbInstance,
            String groupCode) {
        super(type, sdsbInstance);

        this.groupCode = groupCode;
    }

    public String getGroupCode() {
        return groupCode;
    }

    @Override
    protected String[] getFieldsForStringFormat() {
        return new String[] { groupCode };
    }
}
