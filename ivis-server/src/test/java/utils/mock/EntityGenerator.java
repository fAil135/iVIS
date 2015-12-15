package utils.mock;

import com.imcode.entities.*;
import com.imcode.entities.embed.Email;
import com.imcode.entities.embed.Phone;
import com.imcode.entities.interfaces.JpaPersonalizedEntity;
import com.imcode.entities.superclasses.ContactInformation;
import com.imcode.utils.StaticUtils;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by vitaly on 14.12.15.
 */
public interface EntityGenerator<T> extends Iterator<T> {
    public T next();
}

abstract class PersonalizedEntityGenerator<T extends JpaPersonalizedEntity> implements EntityGenerator<T> {
    protected final Random random = new Random();
    protected final EntityGenerator<Person> personGenerator;
    protected final Supplier<T> entitySupplier;
    protected final List<T> generatedEntities = new LinkedList<>();

    public PersonalizedEntityGenerator(EntityGenerator<Person> personGenerator, Supplier<T> entitySupplier) {
        this.personGenerator = personGenerator;
        this.entitySupplier = entitySupplier;
    }

    public List<T> getGeneratedEntities() {
        return Collections.unmodifiableList(generatedEntities);
    }

    @Override
    public T next() {
        return next(true);
    }

    @Override
    public boolean hasNext() {
        return personGenerator.hasNext();
    }

    protected abstract void setProperties(T entity);

    protected T createEntity(boolean full) {
        T entity = entitySupplier.get();

        if (full) {
            setProperties(entity);
        }

        return entity;
    }

    protected boolean whetherCanSet(EntityGenerator<?> entityGenerator) {
//        return random.nextBoolean() && entityGenerator != null && entityGenerator.hasNext();
        return entityGenerator != null && entityGenerator.hasNext();
    }

    protected <E> EntityGenerator<E> wrapNestedEntityGenerator(EntityGenerator<E> nestedEntityGenerator, Supplier<E> nextMethod) {
        return new EntityGenerator<E>() {
            @Override
            public E next() {
                return nextMethod.get();
            }

            @Override
            public boolean hasNext() {
                return nestedEntityGenerator.hasNext();
            }
        };
    }

    protected T next(boolean setProperties) {
        T entity = null;

        if (personGenerator.hasNext()) {
            Person person = personGenerator.next();
            entity = createEntity(setProperties);
            entity.setPerson(person);
            generatedEntities.add(entity);
        }

        return entity;
    }

    protected <E> Set<E> getNestedEntities(EntityGenerator<E> entityGenerator, int minCount, int maxCount) {
        if (minCount >= maxCount)
            throw new IllegalArgumentException("Bad range of min & max values");
        int entityCount = minCount + random.nextInt(maxCount - minCount);
        Set<E> entitySet = new HashSet<>();

        for (int i = 0; i < entityCount && entityGenerator.hasNext(); i++) {
            E entity = entityGenerator.next();
            entitySet.add(entity);
        }

        return entitySet;
    }
}

class WriteHelper {
    private final Path basePath;
    private final String[] baseNames;

    WriteHelper(Path basePath, String[] baseNames) {
        this.basePath = basePath;
        this.baseNames = baseNames;
    }

    public<E> Path write(String fileName, List<E> entityList, String... aditionalColumnNames) {
        Path filePath = basePath.resolve(fileName).normalize();
        String[] pupilsColumnNames = Stream.concat(Arrays.stream(baseNames), Arrays.stream(aditionalColumnNames)).toArray(String[]::new);
        IvisFlatFileItemWriter<E> pupilWriter = new IvisFlatFileItemWriter<>(filePath, pupilsColumnNames);
        pupilWriter.write(entityList);

        return filePath;
    }
    public static void main(String[] args) throws Exception {
        String fileName =  "/home/vitaly/Загрузки/personList.dat";
        List<Person> persons = StaticUtils.loadObjectFromFile(fileName);

        PersonGenerator personGenerator = new PersonGenerator(persons);
        GuardianGenerator guardianGenerator = new GuardianGenerator(personGenerator);
        PupilGenerator pupilGenerator = new PupilGenerator(personGenerator);
        guardianGenerator.setPupilGenerator(pupilGenerator);
        pupilGenerator.setGuardianGenerator(guardianGenerator);

        Random rand = new Random();
        List<Person> personList = new ArrayList<>();
        List<Pupil> pupilList = new ArrayList<>();
        List<Guardian> guardianList = new ArrayList<>();

        while (personGenerator.hasNext()) {
            int x = rand.nextInt(3);
            switch (x) {
                case 0:
                    personList.add(personGenerator.next());
                    break;
                case 1:
                    pupilList.add(pupilGenerator.next());
                    break;
                case 2:
                    guardianList.add(guardianGenerator.next());
                    break;
            }
        }

//        System.out.printf("Person count: %d, Pupil count: %d, Guardian count: %d %n", personList.size(), pupilList.size(), guardianList.size());

        Path personsFile = Paths.get(fileName).resolveSibling("./Persons.csv").normalize();
        String[] personColumnNames = "personalId,firstName,lastName,phones[HOME].value,emails[HOME].value,addresses[REGISTERED].city,addresses[REGISTERED].careOf,addresses[REGISTERED].street,addresses[REGISTERED].street2,addresses[REGISTERED].postalCode,addresses[REGISTERED].municipalityCode".split(",");
        IvisFlatFileItemWriter<Person> personWriter = new IvisFlatFileItemWriter<>(personsFile, personColumnNames);
        personWriter.write(personList);
        Files.readAllLines(personsFile).forEach(System.out::println);
        System.out.println("===================================================================================================================");

        String[] personalizedColumnNames = Arrays.stream(personColumnNames).map("person."::concat).toArray(String[]::new);
        WriteHelper helper = new WriteHelper(Paths.get(fileName).resolveSibling("./").normalize(), personalizedColumnNames);
        Path file;

        file = helper.write("Pupils.csv", pupilGenerator.getGeneratedEntities(), "guardians");
        Files.readAllLines(file).forEach(System.out::println);
        System.out.println("===================================================================================================================");

        file = helper.write("Guardians.csv", guardianGenerator.getGeneratedEntities(), "pupils");
        Files.readAllLines(file).forEach(System.out::println);
        System.out.println("===================================================================================================================");

//        String[] pupilAditionalColumnNames = {"guardians"};
//        Path pupilsFile = Paths.get(fileName).resolveSibling("./Pupils.csv").normalize();
//        String[] pupilsColumnNames = Stream.concat(Arrays.stream(personalizedColumnNames), Arrays.stream(pupilAditionalColumnNames)).toArray(String[]::new);
//        IvisFlatFileItemWriter<Pupil> pupilWriter = new IvisFlatFileItemWriter<>(pupilsFile, pupilsColumnNames);
//        pupilWriter.write(pupilGenerator.getGeneratedEntities());

//        Files.readAllLines(personsFile).forEach(System.out::println);


    }
}


class PupilGenerator extends PersonalizedEntityGenerator<Pupil> {
    private GuardianGenerator guardianGenerator;
    private EntityGenerator<School> schoolGenerator;
    private EntityGenerator<AfterSchoolCenterSection> afterSchoolCenterSectionGenerator;
    private EntityGenerator<Truancy> truancyGenerator;
    private EntityGenerator<AcademicYear> academicYearGenerator;


    public PupilGenerator(EntityGenerator<Person> personGenerator) {
        super(personGenerator, Pupil::new);
    }

    @Override
    protected void setProperties(Pupil entity) {
        setGuardians(entity);
    }

    private void setGuardians(Pupil pupil) {
        if (whetherCanSet(guardianGenerator)) {
            Set<Pupil> pupils = Collections.singleton(pupil);
            EntityGenerator<Guardian> nestedGuardianGenerator = wrapNestedEntityGenerator(guardianGenerator, () -> guardianGenerator.nextWithPupils(pupils));
            Set<Guardian> guardianSet = getNestedEntities(nestedGuardianGenerator, 1, 3);
            guardianSet.stream().forEach(guardian -> guardian.getPupils().add(pupil));
            pupil.setGuardians(guardianSet);
        }
    }

    public EntityGenerator<Guardian> getGuardianGenerator() {
        return guardianGenerator;
    }

    public void setGuardianGenerator(GuardianGenerator guardianGenerator) {
        this.guardianGenerator = guardianGenerator;
    }

    public Pupil nextWithGuardians(Set<Guardian> guardianSet) {
        Pupil entity = next(false);

        if (entity != null) {
            entity.getGuardians().addAll(guardianSet);
        }

        return entity;
    }
}

class GuardianGenerator extends PersonalizedEntityGenerator<Guardian> {
    private PupilGenerator pupilGenerator;

    protected GuardianGenerator(EntityGenerator<Person> personGenerator) {
        super(personGenerator, Guardian::new);
    }

    protected void setProperties(Guardian guardian) {
        //set guardians from person pool, if random boolean and pool has available elements
        setPupils(guardian);
    }

    private void setPupils(Guardian guardian) {
        if (whetherCanSet(pupilGenerator)) {
            Set<Guardian> guardians = Collections.singleton(guardian);
            EntityGenerator<Pupil> nestedPupilGenerator = wrapNestedEntityGenerator(pupilGenerator, () -> pupilGenerator.nextWithGuardians(guardians));
            Set<Pupil> pupilSet = getNestedEntities(nestedPupilGenerator, 1, 4);
            pupilSet.forEach(pupil -> pupil.getGuardians().add(guardian));
            guardian.setPupils(pupilSet);
        }
    }

    public EntityGenerator<Pupil> getPupilGenerator() {
        return pupilGenerator;
    }

    public void setPupilGenerator(PupilGenerator pupilGenerator) {
        this.pupilGenerator = pupilGenerator;
    }

    public Guardian nextWithPupils(Set<Pupil> pupilSet) {
        Guardian entity = next(false);

        if (entity != null) {
            entity.getPupils().addAll(pupilSet);
        }

        return entity;
    }
}

class PersonGenerator implements EntityGenerator<Person> {
    private final Queue<Person> personQueue;

    PersonGenerator(Collection<Person> persons) {
        this.personQueue = new LinkedBlockingDeque<>(persons);
//        this.personQueue.addAll(persons);
    }

    @Override
    public boolean hasNext() {
        return personQueue.size() > 0;
    }

    @Override
    public Person next() {
        return personQueue.poll();
    }
}


class IvisFlatFileItemWriter<T> extends FlatFileItemWriter<T> {
    private boolean writeHeader = false;
    private String[] columnNames;
    private ConfigurableConversionService conversionService;

    private class IvisDelimitedLineAggregator<T> extends DelimitedLineAggregator<T> {
//        private final ConversionService conversionService;

//        IvisDelimitedLineAggregator(ConversionService conversionService) {
//            this.conversionService = conversionService;
//        }

        @Override
        public String doAggregate(Object[] fields) {
            String[] stringFields = convertToString(fields);
            return super.doAggregate(stringFields);
        }

        private String[] convertToString(Object[] fields) {
            String[] stringFields = new String[fields.length];

            for (int i = 0; i < fields.length; i++) {
                Object field = fields[i];
                String stringField = convertIfNecessary(field, String.class);
                stringFields[i] = stringField;
            }

            return stringFields;
        }

        @SuppressWarnings("unchecked")
        private <T> T convertIfNecessary(Object value, Class<T> targetClass) {
            if (targetClass == value.getClass()) {
                return (T) value;
            }

            Object result = conversionService.convert(value, targetClass);

            return convertIfNecessary(result, targetClass);
        }
    }

    public IvisFlatFileItemWriter(Path path, String... columnNames) {
        this(new FileSystemResource(path.toFile()), columnNames, true);
    }

    public IvisFlatFileItemWriter(Resource resource, String[] columnNames, boolean writeHeaders) {
        Objects.requireNonNull(columnNames);
        this.setResource(resource);
        this.columnNames = columnNames;
        this.writeHeader = writeHeaders;
        this.conversionService = getDefaultConversionService();

        BeanWrapperFieldExtractor<T> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(columnNames);

        IvisDelimitedLineAggregator<T> lineAggregator = new IvisDelimitedLineAggregator<>();
        lineAggregator.setFieldExtractor(fieldExtractor);

        this.setLineAggregator(lineAggregator);
        this.setWriteHeaders(writeHeaders);
//        this.setShouldDeleteIfExists(true);

    }

    public static ConfigurableConversionService getDefaultConversionService() {
        DefaultConversionService conversionService = new DefaultConversionService();
        Converter<ContactInformation<?>, String> contactInformationConverter = ContactInformation::getValue;
        Converter<JpaPersonalizedEntity, String> personalizedConverter = source -> source.getPerson().getPersonalId();

        conversionService.addConverter(Phone.class, String.class, contactInformationConverter);
        conversionService.addConverter(Email.class, String.class, contactInformationConverter);
        conversionService.addConverter(Pupil.class, String.class, personalizedConverter);
        conversionService.addConverter(Person.class, String.class, personalizedConverter);
        conversionService.addConverter(Guardian.class, String.class, personalizedConverter);
        conversionService.addConverter(EnumMap.class, String.class, (Converter<EnumMap, Collection>) EnumMap::values);

        return conversionService;
    }

    public boolean isWriteHeaders() {
        return writeHeader;
    }

    public void setWriteHeaders(boolean writeHeader) {
        this.writeHeader = writeHeader;

        if (writeHeader) {
            setHeaderCallback(w -> w.append(Arrays.stream(columnNames).collect(Collectors.joining(","))));
        } else {
            setHeaderCallback(null);
        }
    }

    @Override
    public void write(List<? extends T> items) {
        try {
            this.open(new ExecutionContext());
            super.write(items);
            this.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ConfigurableConversionService getConversionService() {
        return conversionService;
    }

    public void setConversionService(ConfigurableConversionService conversionService) {
        this.conversionService = conversionService;
    }

    public <S, E> void addConvertor(Class<S> sourceType, Class<E> targetType, Converter<S, E> converter) {
        conversionService.addConverter(sourceType, targetType, converter);
    }
}
