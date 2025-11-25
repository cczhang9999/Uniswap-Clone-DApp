import "../styles/globals.css";
import { SwapProvider } from "../Context/SwapContext";
import NavBar from "../Components/NavBar/NavBar";

const MyApp = ({ Component, pageProps }) => (
  <div>
    <SwapProvider>
      <NavBar />
      <Component {...pageProps} />
    </SwapProvider>
  </div>
);

export default MyApp;
