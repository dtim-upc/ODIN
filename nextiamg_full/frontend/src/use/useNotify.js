import { Notify } from 'quasar';

// Taken from https://stackoverflow.com/questions/67983127/how-can-i-set-some-global-functions-mixins-in-quasar-vue-v2-framework

export function useNotify() {

    const negative = (message) => {
        Notify.create({
          message,
          color: "negative",
          icon: "cancel",
          textColor: "white",
        });
      };
      
      const positive = (message) => {
        Notify.create({
          message,
          color: "positive",
          textColor: "white",
          icon: "check_circle",
        });
      };

      return { negative, positive}

 }

