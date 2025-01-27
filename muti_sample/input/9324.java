public final class Test6921644 extends AbstractTest {
    public static void main(String[] args) {
        new Test6921644().test(true);
    }
    protected Object getObject() {
        Owner<Author> o = new Owner<Author>(100); 
        Category c = new Category(o);
        Document d1 = new Document(o);
        Document d2 = new Document(o);
        Document d3 = new Document(o);
        Author a1 = new Author(o);
        Author a2 = new Author(o);
        Author a3 = new Author(o);
        o.getList().add(a1);
        o.getList().add(a2);
        o.getList().add(a3);
        a3.setRef(o.getId());
        d2.setCategory(c);
        d3.setCategory(c);
        a1.addDocument(d1);
        a1.addDocument(d2);
        a3.addDocument(d3);
        c.addDocument(d2);
        c.addDocument(d3);
        return o;
    }
    public static class Owner<T> {
        private int id;
        private List<T> list = new ArrayList<T>();
        @ConstructorProperties("id")
        public Owner(int id) {
            this.id = id;
        }
        public int getId() {
            return this.id;
        }
        public List<T> getList() {
            return this.list;
        }
        public void setList(List<T> list) {
            this.list = list;
        }
    }
    public static class Author {
        private int id;
        private int ref;
        private Owner owner;
        private List<Document> list = new ArrayList<Document>();
        @ConstructorProperties("owner")
        public Author(Owner<Author> owner) {
            this.owner = owner;
            this.id = owner.getId();
        }
        public Owner getOwner() {
            return this.owner;
        }
        public Integer getId() {
            return this.id;
        }
        public void setId(Integer id) {
            this.id = id;
        }
        public Integer getRef() {
            return this.ref;
        }
        public void setRef(Integer ref) {
            this.ref = ref;
        }
        public List<Document> getList() {
            return this.list;
        }
        public void setList(List<Document> list) {
            this.list = list;
        }
        public void addDocument(Document document) {
            this.list.add(document);
            document.setAuthor(this);
        }
    }
    public static class Category {
        private Owner owner;
        private List<Document> list = new ArrayList<Document>();
        @ConstructorProperties("owner")
        public Category(Owner owner) {
            this.owner = owner;
        }
        public Owner getOwner() {
            return this.owner;
        }
        public List<Document> getList() {
            return this.list;
        }
        public void setList(List<Document> list) {
            this.list = list;
        }
        public void addDocument(Document document) {
            this.list.add(document);
            document.setCategory(this);
        }
    }
    public static class Document {
        private Owner owner;
        private Author author;
        private Category category;
        @ConstructorProperties("owner")
        public Document(Owner owner) {
            this.owner = owner;
        }
        public Owner getOwner() {
            return this.owner;
        }
        public Author getAuthor() {
            return this.author;
        }
        public void setAuthor(Author author) {
            this.author = author;
        }
        public Category getCategory() {
            return this.category;
        }
        public void setCategory(Category category) {
            this.category = category;
        }
    }
}
