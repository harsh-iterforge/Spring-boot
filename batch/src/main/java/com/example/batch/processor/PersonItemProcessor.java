package com.example.batch.processor;


import com.example.batch.model.Person;
import org.springframework.batch.item.ItemProcessor;


public class PersonItemProcessor implements ItemProcessor<Person, Person> {


    @Override
    public Person process(Person person) throws Exception {
// Example transformation: trim name and lower-case email
        if (person.getName() != null) {
            person.setName(person.getName().trim());
        }
        if (person.getEmail() != null) {
            person.setEmail(person.getEmail().trim().toLowerCase());
        }
        return person;
    }
}