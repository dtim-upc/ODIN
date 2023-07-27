import { boot } from 'quasar/wrappers'
import Particles from "vue3-particles";
// // "async" is optional;
// // more info on params: https://v2.quasar.dev/quasar-cli/boot-files
export default ( ( {app}) => {


  app.use(Particles)

})

// how to add external script
// https://quasar.dev/quasar-plugins/meta